package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.ChatMessageDto;
import core.hackathon02api.auth.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatHistoryController {
    private final ChatMessageService chatMessageService;

    @GetMapping("/{roomId}/messages")
    public List<ChatMessageDto> getMessages(@PathVariable Long roomId) {
        return chatMessageService.findRecentMessages(roomId, 50) // 최근 50개
                .stream()
                .map(m -> ChatMessageDto.builder()
                        .messageId(m.getId())   // ★ 새 필드 추가
                        .roomId(m.getRoom().getId())
                        .senderId(m.getSender().getId())
                        .senderNickname(m.getSender().getNickname())
                        .content(m.getContent())
                        .createdAt(m.getCreatedAt().toLocalDateTime())
                        .build()
                )
                .toList();
    }
}
