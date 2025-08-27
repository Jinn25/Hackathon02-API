package core.hackathon02api.auth.service;

import core.hackathon02api.auth.dto.NotificationItemResponse;
import core.hackathon02api.auth.dto.NotificationListResponse;
import core.hackathon02api.auth.dto.NotificationUpdatesResponse;
import core.hackathon02api.auth.entity.*;
import core.hackathon02api.auth.repository.NotificationRepository;
import core.hackathon02api.auth.repository.PostApplicationRepository;
import core.hackathon02api.auth.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final List<ApplicationStatus> COUNT_STATUSES =
            List.of(ApplicationStatus.APPROVED); // 승인 인원 집계

    private final NotificationRepository notificationRepository;
    private final PostRepository postRepository;
    private final PostApplicationRepository postApplicationRepository;

    @Transactional(readOnly = true)
    public NotificationListResponse listAll(Long userId, Boolean isRead, List<NotificationType> types) {
        boolean hasRead = isRead != null;
        boolean hasTypes = types != null && !types.isEmpty();

        List<Notification> rows;
        if (!hasRead && !hasTypes) {
            rows = notificationRepository.findByUserIdOrderByIdDesc(userId);
        } else if (hasRead && !hasTypes) {
            rows = notificationRepository.findByUserIdAndIsReadOrderByIdDesc(userId, isRead);
        } else if (!hasRead && hasTypes) {
            rows = notificationRepository.findByUserIdAndTypeInOrderByIdDesc(userId, types);
        } else {
            rows = notificationRepository.findByUserIdAndIsReadAndTypeInOrderByIdDesc(userId, isRead, types);
        }

        return toListResponseWithPostAugment(rows);
    }

    @Transactional
    public NotificationItemResponse markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));

        // 읽음 처리
        n.markRead(Instant.now());

        // 확장 필드 기본값
        String postTitle = null;
        Integer desired = null;
        Integer current = null;
        String productDesc = null;
        String imageUrl = null;

        Long refPostId = n.getRefPostId();
        if (refPostId != null) {
            Post p = postRepository.findById(refPostId).orElse(null);
            if (p != null) {
                postTitle = p.getTitle();
                desired = Optional.ofNullable(p.getDesiredMemberCount()).orElse(0);

                // 승인 인원 + 작성자 1명
                long approved = postApplicationRepository
                        .countByPost_IdAndStatusIn(refPostId, COUNT_STATUSES);
                current = (int) approved + 1;

                // 설명/이미지
                try {
                    productDesc = p.getProductDesc();
                } catch (Exception ignore) { /* Post에 없으면 무시 */ }

                if (p.getMainImageUrl() != null && !p.getMainImageUrl().isBlank()) {
                    imageUrl = p.getMainImageUrl();
                }
                // (예전 스키마 호환이 필요하면)
                // else if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
                //     imageUrl = p.getImageUrls().get(0);
                // }
            }
        }

        return new NotificationItemResponse(
                n.getId(),
                n.getType().name(),
                n.getTitle(),
                n.getMessage(),
                refPostId,
                postTitle,
                current,
                desired,
                productDesc,
                imageUrl,
                n.isRead(),
                n.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public NotificationUpdatesResponse updates(Long userId, Instant since) {
        // 1) 새 알림 조회
        List<Notification> rows = notificationRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since);

        // 2) refPostId 모아 한번에 포스트/인원수 로딩
        List<Long> postIds = rows.stream()
                .map(Notification::getRefPostId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Post> postsById = postIds.isEmpty()
                ? Map.of()
                : postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        Map<Long, Integer> currentByPost = postIds.isEmpty() ? Map.of() :
                postApplicationRepository.countApprovedByPostIds(postIds, COUNT_STATUSES).stream()
                        .collect(Collectors.toMap(
                                PostApplicationRepository.PostCountProjection::getPostId,
                                prj -> (int) (prj.getCnt() + 1) // 작성자 +1
                        ));

        // 3) 확장 필드 채워서 DTO 매핑
        List<NotificationItemResponse> items = rows.stream().map(n -> {
            Post p = n.getRefPostId() == null ? null : postsById.get(n.getRefPostId());
            String postTitle = p != null ? p.getTitle() : null;
            Integer desired = p != null ? Optional.ofNullable(p.getDesiredMemberCount()).orElse(0) : null;
            Integer current = p != null ? currentByPost.getOrDefault(p.getId(), null) : null;

            String productDesc = null;
            if (p != null) {
                try {
                    var m = Post.class.getMethod("getProductDesc");
                    Object v = m.invoke(p);
                    if (v instanceof String s) productDesc = s;
                } catch (Exception ignore) {}
            }

            String imageUrl = null;
            if (p != null) {
                if (p.getMainImageUrl() != null && !p.getMainImageUrl().isBlank()) {
                    imageUrl = p.getMainImageUrl();
                }
            }

            return new NotificationItemResponse(
                    n.getId(),
                    n.getType().name(),
                    n.getTitle(),
                    n.getMessage(),
                    n.getRefPostId(),
                    postTitle,
                    current,
                    desired,
                    productDesc,
                    imageUrl,
                    n.isRead(),
                    n.getCreatedAt()
            );
        }).toList();

        long unread = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return NotificationUpdatesResponse.builder()
                .data(items)
                .totalUnread(unread)
                .serverTime(Instant.now())
                .build();
    }


    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // ===== 내부 공통 매핑 =====
    private NotificationListResponse toListResponseWithPostAugment(List<Notification> rows) {
        List<Long> postIds = rows.stream()
                .map(Notification::getRefPostId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Post> postsById = postIds.isEmpty()
                ? Map.of()
                : postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        Map<Long, Integer> currentByPost = postIds.isEmpty() ? Map.of() :
                postApplicationRepository.countApprovedByPostIds(postIds, COUNT_STATUSES).stream()
                        .collect(Collectors.toMap(
                                PostApplicationRepository.PostCountProjection::getPostId,
                                prj -> (int) (prj.getCnt() + 1) // 작성자 +1
                        ));

        List<NotificationItemResponse> items = rows.stream().map(n -> {
            Post p = n.getRefPostId() == null ? null : postsById.get(n.getRefPostId());
            String postTitle = p != null ? p.getTitle() : null;
            Integer desired = p != null ? Optional.ofNullable(p.getDesiredMemberCount()).orElse(0) : null;
            Integer current = p != null ? currentByPost.getOrDefault(p.getId(), null) : null;

            String productDesc = null;
            if (p != null) {
                try {
                    var m = Post.class.getMethod("getProductDesc"); // 선택 필드
                    Object v = m.invoke(p);
                    if (v instanceof String s) productDesc = s;
                } catch (Exception ignore) {}
            }

            String imageUrl = null;
            if (p != null) {
                if (p.getMainImageUrl() != null && !p.getMainImageUrl().isBlank()) {
                    imageUrl = p.getMainImageUrl();
                } else if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) {
                    imageUrl = p.getImageUrls().get(0);
                }
            }

            return new NotificationItemResponse(
                    n.getId(),
                    n.getType().name(),
                    n.getTitle(),
                    n.getMessage(),
                    n.getRefPostId(),
                    postTitle,
                    current,
                    desired,
                    productDesc,
                    imageUrl,
                    n.isRead(),
                    n.getCreatedAt()
            );
        }).toList();

        return new NotificationListResponse(items);
    }

    // NotificationService 내부에 추가
    @Transactional
    public Long notify(Long userId, NotificationType type, String title, String message, Long refPostId) {
        Notification saved = notificationRepository.save(
                Notification.builder()
                        .userId(userId)
                        .type(type)
                        .title(title)
                        .message(message)
                        .refPostId(refPostId)
                        .isRead(false)
                        .build()
        );
        return saved.getId();
    }

}
