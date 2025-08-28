package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.ChatMessageDto;
import core.hackathon02api.auth.entity.ChatMessage;
import core.hackathon02api.auth.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.security.core.Authentication;
import org.springframework.web.server.ResponseStatusException;

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
    public List<ChatMessageDto> getMessages(@PathVariable Long roomId, Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        //Long userId = Long.valueOf((String) authentication.getPrincipal());

//        // ✅ 마지막 메시지 ID 조회
//        ChatMessage last = chatMessageRepository.findTopByRoom_IdOrderByIdDesc(roomId).orElse(null);
//        if (last != null) {
//            myChatRoomService.markRead(userId, roomId, last.getId());
//        }

        return chatMessageService.findRecentMessagesDto(roomId, 50);
    }

}
