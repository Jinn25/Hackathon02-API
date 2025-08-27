package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.NotificationItemResponse;
import core.hackathon02api.auth.dto.NotificationListResponse;
import core.hackathon02api.auth.dto.NotificationUpdatesResponse;
import core.hackathon02api.auth.entity.NotificationType;
import core.hackathon02api.auth.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * 알림 Full-Fetch 전용 컨트롤러
 * - X-USER-ID 헤더로 사용자 식별(운영에서는 토큰 기반 권장)
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 전체 조회 (isRead, types는 선택)
    // 예: GET /api/notifications
    //     GET /api/notifications?isRead=false
    //     GET /api/notifications?types=APPROVED,REJECTED
    @GetMapping
    public ResponseEntity<NotificationListResponse> listAll(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) List<NotificationType> types
    ) {
        var resp = notificationService.listAll(userId, isRead, types);
        return ResponseEntity.ok(resp);
    }

    // 단건 읽음 처리
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<NotificationItemResponse> markRead(
            @RequestHeader("X-USER-ID") Long userId,
            @PathVariable Long notificationId
    ) {
        var resp = notificationService.markRead(userId, notificationId);
        return ResponseEntity.ok(resp);
    }

    // 폴링(새 알림)
    // 예: GET /api/notifications/updates?since=2025-08-20T00:00:00Z
    @GetMapping("/updates")
    public ResponseEntity<NotificationUpdatesResponse> updates(
            @RequestHeader("X-USER-ID") Long userId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since
    ) {
        var resp = notificationService.updates(userId, since);
        return ResponseEntity.ok(resp);
    }

    // 미읽음 카운트
    @GetMapping("/unread/count")
    public ResponseEntity<Long> unreadCount(
            @RequestHeader("X-USER-ID") Long userId
    ) {
        long cnt = notificationService.countUnread(userId);
        return ResponseEntity.ok(cnt);
    }
}
