package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.service.ChatRoomService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatReadCursorWsController {

    private final ChatRoomService chatRoomService;

    // 클라이언트 publish: /pub/chatrooms/{roomId}/read
    @MessageMapping("/chatrooms/{roomId}/read")
    public void updateReadCursorViaWs(
            @DestinationVariable Long roomId,
            @Payload ReadCursorMsg msg,
            Principal principal
    ) {
        // WS 인증에서 Principal.getName()을 userId 문자열로 세팅했다고 가정
        Long userId = Long.valueOf(principal.getName());
        if (msg != null && msg.getLastReadMessageId() != null) {
            chatRoomService.updateReadCursor(userId, roomId, msg.getLastReadMessageId());
        }
    }

    @Data
    public static class ReadCursorMsg {
        private Long lastReadMessageId;
    }
}