package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.ChatMessageDto;
import core.hackathon02api.auth.entity.ChatMessage;
import core.hackathon02api.auth.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Comparator;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ChatMessageController {
//    private final SimpMessagingTemplate messagingTemplate;
//    private final ChatMessageService chatMessageService;
//
//    @MessageMapping("/chatrooms/{roomId}/send")
//    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto messageDto) {
//        ChatMessage saved = chatMessageService.saveMessage(roomId, messageDto.getSenderId(), messageDto.getContent());
//
//        ChatMessageDto response = new ChatMessageDto(
//                saved.getRoom().getId(),
//                saved.getSender().getId(),
//                saved.getContent(),
//                saved.getCreatedAt().toLocalDateTime()
//        );
//
//        messagingTemplate.convertAndSend("/sub/chatrooms/" + roomId, response);
//    }
    private final ChatMessageService chatMessageService;

    @MessageMapping("/chatrooms/{roomId}/send")
    public void sendMessage(@DestinationVariable Long roomId, ChatMessageDto dto) {
        chatMessageService.saveAndBroadcast(roomId, dto);
    }

    @GetMapping("/chatrooms/{roomId}/messages")
    @ResponseBody
    public List<ChatMessageDto> getMessages(@PathVariable Long roomId) {
        return chatMessageService.findRecentMessagesDto(roomId, 50);
    }

}
