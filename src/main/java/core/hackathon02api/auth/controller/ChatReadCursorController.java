package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.service.ChatRoomService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatReadCursorController {

    private final ChatRoomService chatRoomService;

    // 프런트 기본 호출 (PATCH)
    @PatchMapping("/{roomId}/read-cursor")
    public ResponseEntity<Void> patchReadCursor(
            @PathVariable Long roomId,
            @RequestBody ReadCursorRequest request,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        chatRoomService.updateReadCursor(userId, roomId, request.getLastReadMessageId());
        return ResponseEntity.noContent().build(); // 204
    }

    // beforeunload에서 sendBeacon용 (POST 허용, body 동일)
    @PostMapping("/{roomId}/read-cursor")
    public ResponseEntity<Void> postReadCursor(
            @PathVariable Long roomId,
            @RequestBody ReadCursorRequest request,
            Authentication authentication
    ) {
        Long userId = extractUserId(authentication);
        chatRoomService.updateReadCursor(userId, roomId, request.getLastReadMessageId());
        return ResponseEntity.noContent().build();
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "unauthorized");
        }
        // 기존 AuthController 등과 동일한 형태(커스텀 필터가 principal에 userId 문자열 넣는 구조)
        return Long.valueOf((String) authentication.getPrincipal());
    }

    @Data
    public static class ReadCursorRequest {
        private Long lastReadMessageId;
    }
}