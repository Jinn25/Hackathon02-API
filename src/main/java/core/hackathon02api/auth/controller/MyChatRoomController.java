package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.ChatRoomEnterResponse;
import core.hackathon02api.auth.dto.MarkReadRequest;
import core.hackathon02api.auth.dto.MyChatRoomItem;
import core.hackathon02api.auth.service.MyChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MyChatRoomController {

    private final MyChatRoomService myChatRoomService;

    /** 내 채팅방 전체 목록 (정렬: 마지막 활동 내림차순) */
    @GetMapping("/api/me/chatrooms")
    public ResponseEntity<List<MyChatRoomItem>> listMyRooms(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = Long.valueOf((String) authentication.getPrincipal());
        return ResponseEntity.ok(myChatRoomService.listMyRooms(userId));
    }

    /** 방 열기 (roomId 기준) */
    @GetMapping("/api/chatrooms/{roomId}")
    public ResponseEntity<ChatRoomEnterResponse> open(
            @PathVariable Long roomId,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = Long.valueOf((String) authentication.getPrincipal());
        return ResponseEntity.ok(myChatRoomService.open(userId, roomId));
    }

    /** 읽음 처리 (현재 클라가 본 마지막 메시지 ID까지) */
    @PatchMapping("/api/chatrooms/{roomId}/read")
    public ResponseEntity<Void> markRead(
            @PathVariable Long roomId,
            @RequestBody MarkReadRequest req,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = Long.valueOf((String) authentication.getPrincipal());
        myChatRoomService.markRead(userId, roomId, req.getLastSeenMessageId());
        return ResponseEntity.noContent().build();
    }
}