package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.NotificationItemResponse;
import core.hackathon02api.auth.dto.NotificationUpdatesResponse;
import core.hackathon02api.auth.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 조회
    @GetMapping
    public List<NotificationItemResponse> list(@AuthenticationPrincipal(expression = "id") Long userId) {
        return notificationService.list(userId);
    }

    // 알림 읽음 처리
    @PostMapping("/{notificationId}/read")
    public NotificationItemResponse markRead(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @PathVariable Long notificationId
    ) {
        return notificationService.markRead(userId, notificationId);
    }

    // 새로고침(폴링)
    @GetMapping("/updates")
    public NotificationUpdatesResponse updates(
            @AuthenticationPrincipal(expression = "id") Long userId,
            @RequestParam Instant since
    ) {
        return notificationService.updates(userId, since);
    }
}
