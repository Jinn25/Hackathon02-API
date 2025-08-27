package core.hackathon02api.auth.service;

import core.hackathon02api.auth.dto.NotificationItemResponse;
import core.hackathon02api.auth.dto.NotificationListResponse;
import core.hackathon02api.auth.dto.NotificationUpdatesResponse;
import core.hackathon02api.auth.entity.*;
import core.hackathon02api.auth.repository.NotificationRepository;
import core.hackathon02api.auth.repository.PostApplicationRepository;
import core.hackathon02api.auth.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final List<ApplicationStatus> COUNT_STATUSES =
            List.of(ApplicationStatus.APPROVED/*, ApplicationStatus.JOINED*/); // 승인으로 집계

    private final NotificationRepository notificationRepository;
    private final PostRepository postRepository;
    private final PostApplicationRepository postApplicationRepository;

    // 무한 스크롤 목록
    @Transactional(readOnly = true)
    public NotificationListResponse list(Long userId, Long lastId, Integer limit,
                                         Boolean isRead, List<NotificationType> types) {

        int pageSize = (limit == null ? 20 : Math.min(Math.max(1, limit), 100));
        var pageable = PageRequest.of(0, pageSize);

        List<Notification> rows;
        boolean hasLast = lastId != null;
        boolean hasRead = isRead != null;
        boolean hasTypes = types != null && !types.isEmpty();

        if (!hasLast && !hasRead && !hasTypes) {
            rows = notificationRepository.findByUserIdOrderByIdDesc(userId, pageable);
        } else if (hasLast && !hasRead && !hasTypes) {
            rows = notificationRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, lastId, pageable);
        } else if (!hasLast && hasRead && !hasTypes) {
            rows = notificationRepository.findByUserIdAndIsReadOrderByIdDesc(userId, isRead, pageable);
        } else if (hasLast && hasRead && !hasTypes) {
            rows = notificationRepository.findByUserIdAndIsReadAndIdLessThanOrderByIdDesc(userId, isRead, lastId, pageable);
        } else if (!hasLast && !hasRead && hasTypes) {
            rows = notificationRepository.findByUserIdAndTypeInOrderByIdDesc(userId, types, pageable);
        } else if (hasLast && !hasRead && hasTypes) {
            rows = notificationRepository.findByUserIdAndTypeInAndIdLessThanOrderByIdDesc(userId, types, lastId, pageable);
        } else if (!hasLast && hasRead && hasTypes) {
            rows = notificationRepository.findByUserIdAndIsReadAndTypeInOrderByIdDesc(userId, isRead, types, pageable);
        } else { // hasLast && hasRead && hasTypes
            rows = notificationRepository.findByUserIdAndIsReadAndTypeInAndIdLessThanOrderByIdDesc(userId, isRead, types, lastId, pageable);
        }

        // refPostId 모아 한번에 조회
        List<Long> postIds = rows.stream()
                .map(Notification::getRefPostId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        Map<Long, Post> postsById = postIds.isEmpty()
                ? Map.of()
                : postRepository.findAllById(postIds).stream()
                .collect(Collectors.toMap(Post::getId, p -> p));

        // 승인 인원 group by 후 +1(작성자)
        Map<Long, Integer> currentByPost = postIds.isEmpty() ? Map.of() :
                postApplicationRepository.countApprovedByPostIds(postIds, COUNT_STATUSES).stream()
                        .collect(Collectors.toMap(
                                PostApplicationRepository.PostCountProjection::getPostId,
                                prj -> (int) (prj.getCnt() + 1) // 작성자 +1
                        ));

        // DTO 매핑
        List<NotificationItemResponse> items = rows.stream().map(n -> {
            Post p = n.getRefPostId() == null ? null : postsById.get(n.getRefPostId());
            String postTitle = p != null ? p.getTitle() : null;
            Integer desired = p != null ? Optional.ofNullable(p.getDesiredMemberCount()).orElse(0) : null;
            Integer current = p != null ? currentByPost.getOrDefault(p.getId(), null) : null;

            String productDesc = null;
            if (p != null) {
                try {
                    var m = Post.class.getMethod("getProductDesc"); // Post 엔티티에 productDesc 필드/메서드 있다고 가정
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

    // 읽음 처리(단건)
    @Transactional
    public NotificationItemResponse markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        n.markRead(Instant.now());

        // 읽음 후의 단건 응답도 확장 필드로 내려주려면 refPost를 붙여서 만들 수도 있음
        return new NotificationItemResponse(
                n.getId(), n.getType().name(), n.getTitle(), n.getMessage(), n.getRefPostId(),
                null, null, null, null, null, // 단건에선 굳이 붙이지 않을 수도 (원하면 동일 로직 재사용)
                n.isRead(), n.getCreatedAt()
        );
    }

    // 새로고침(폴링) – 기존 유지
    @Transactional(readOnly = true)
    public NotificationUpdatesResponse updates(Long userId, Instant since) {
        var items = notificationRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since)
                .stream()
                .map(n -> new NotificationItemResponse(
                        n.getId(), n.getType().name(), n.getTitle(), n.getMessage(), n.getRefPostId(),
                        null, null, null, null, null,
                        n.isRead(), n.getCreatedAt()))
                .toList();

        long unread = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return NotificationUpdatesResponse.builder()
                .data(items)
                .totalUnread(unread)
                .serverTime(Instant.now())
                .build();
    }

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