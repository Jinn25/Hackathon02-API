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
                .map(m -> new ChatMessageDto(
                        m.getRoom().getId(),
                        m.getSender().getId(),
                        m.getSender().getNickname(),
                        m.getContent(),
                        m.getCreatedAt().toLocalDateTime()
                ))
                .toList();
    }
}
