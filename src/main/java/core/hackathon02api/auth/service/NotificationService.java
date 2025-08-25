package core.hackathon02api.auth.service;

import core.hackathon02api.auth.dto.NotificationItemResponse;
import core.hackathon02api.auth.dto.NotificationUpdatesResponse;
import core.hackathon02api.auth.entity.Notification;
import core.hackathon02api.auth.entity.NotificationType;
import core.hackathon02api.auth.repository.NotificationRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    // 목록 조회 (필터 없고 정렬만, 페이징 X)
    @Transactional(readOnly = true)
    public List<NotificationItemResponse> list(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(NotificationItemResponse::from)
                .toList();
    }

    // 읽음 처리 (단건)
    @Transactional
    public NotificationItemResponse markRead(Long userId, Long notificationId) {
        Notification n = notificationRepository.findByIdAndUserId(notificationId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        n.markRead(Instant.now());
        return NotificationItemResponse.from(n);
    }

    // 새로고침 폴링: since 이후 생성분만
    @Transactional(readOnly = true)
    public NotificationUpdatesResponse updates(Long userId, Instant since) {
        var items = notificationRepository
                .findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(userId, since)
                .stream().map(NotificationItemResponse::from)
                .toList();
        long unread = notificationRepository.countByUserIdAndIsReadFalse(userId);
        return NotificationUpdatesResponse.builder()
                .data(items)
                .totalUnread(unread)
                .serverTime(Instant.now())
                .build();
    }

    // 내부에서 사용할 생성 헬퍼 (알림 발송 지점에서 호출)
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