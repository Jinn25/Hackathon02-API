package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.NotificationListResponse;
import core.hackathon02api.auth.dto.NotificationItemResponse;
import core.hackathon02api.auth.dto.NotificationUpdatesResponse;
import core.hackathon02api.auth.entity.NotificationType;
import core.hackathon02api.auth.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    // 알림 목록 (무한 스크롤, hasMore 없음)
    @GetMapping
    public NotificationListResponse list(
            @AuthenticationPrincipal(expression = "id") Long userId,
            Authentication auth,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Boolean isRead,
            @RequestParam(required = false) List<NotificationType> type
    ) {
        Long uid = resolveUserId(userId, auth);
        return notificationService.list(uid, lastId, limit, isRead, type);
    }

    // 알림 읽음 처리
    @PostMapping("/{notificationId}/read")
    public NotificationItemResponse markRead(
            @AuthenticationPrincipal(expression = "id") Long userId,
            Authentication auth,
            @PathVariable Long notificationId
    ) {
        Long uid = resolveUserId(userId, auth);
        return notificationService.markRead(uid, notificationId);
    }

    // 새로고침 폴링
    @GetMapping("/updates")
    public NotificationUpdatesResponse updates(
            @AuthenticationPrincipal(expression = "id") Long userId,
            Authentication auth,
            @RequestParam Instant since
    ) {
        Long uid = resolveUserId(userId, auth);
        return notificationService.updates(uid, since);
    }

    private Long resolveUserId(Long injected, Authentication auth) {
        if (injected != null) return injected;
        if (auth == null || auth.getPrincipal() == null)
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "로그인이 필요합니다.");
        Object p = auth.getPrincipal();
        if (p instanceof String s) {
            try { return Long.valueOf(s); }
            catch (NumberFormatException e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 정보입니다.");
            }
        }
        // 필요 시 CustomUserDetails 캐스팅
        // return ((CustomUserDetails) p).getId();
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "유효하지 않은 인증 주체입니다.");
    }
}
