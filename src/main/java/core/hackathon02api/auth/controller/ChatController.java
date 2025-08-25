package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatController {
//    private final SimpMessagingTemplate messagingTemplate;
//
//    @MessageMapping("/chatrooms/{roomId}/send")
//    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto message) {
//        // DB 저장 로직 추가 가능
//        messagingTemplate.convertAndSend("/sub/chatrooms/" + roomId, message);
//    }
}
