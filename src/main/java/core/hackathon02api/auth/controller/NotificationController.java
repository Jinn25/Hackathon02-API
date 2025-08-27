package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.NotificationItemResponse;
import core.hackathon02api.auth.dto.NotificationListResponse;
import core.hackathon02api.auth.dto.NotificationUpdatesResponse;
import core.hackathon02api.auth.entity.NotificationType;
import core.hackathon02api.auth.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

/**
 * 알림 Full-Fetch 전용 컨트롤러
 * - JWT 토큰(Authentication)에서 userId 추출
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    private Long resolveUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) {
            throw new RuntimeException("Unauthenticated");
        }
        return Long.valueOf((String) auth.getPrincipal()); // JwtAuthenticationFilter에서 principal=userId 문자열로 설정했다고 가정
    }

    // 전체 조회 (isRead, types 선택)
    @GetMapping
    public ResponseEntity<NotificationListResponse> listAll(
            Authentication auth,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) List<NotificationType> types
    ) {
        Long userId = resolveUserId(auth);
        var resp = notificationService.listAll(userId, isRead, types);
        return ResponseEntity.ok(resp);
    }

    // 단건 읽음 처리
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<NotificationItemResponse> markRead(
            Authentication auth,
            @PathVariable Long notificationId
    ) {
        Long userId = resolveUserId(auth);
        var resp = notificationService.markRead(userId, notificationId);
        return ResponseEntity.ok(resp);
    }

    // 폴링(새 알림)
    @GetMapping("/updates")
    public ResponseEntity<NotificationUpdatesResponse> updates(
            Authentication auth,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant since
    ) {
        Long userId = resolveUserId(auth);
        var resp = notificationService.updates(userId, since);
        return ResponseEntity.ok(resp);
    }

    // 미읽음 카운트
    @GetMapping("/unread/count")
    public ResponseEntity<Long> unreadCount(Authentication auth) {
        Long userId = resolveUserId(auth);
        long cnt = notificationService.countUnread(userId);
        return ResponseEntity.ok(cnt);
    }
}
