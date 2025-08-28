package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.service.ChatRoomService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatReadAllController {

    private final ChatRoomService chatRoomService;

    @PostMapping("/{roomId}/read-all")
    public ResponseEntity<Void> readAll(
            @PathVariable Long roomId,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ResponseEntity.status(401).build();
        }
        Long userId = Long.valueOf((String) authentication.getPrincipal()); // 기존 방식 유지
        chatRoomService.markAllRead(userId, roomId);
        return ResponseEntity.noContent().build(); // 204
    }
}