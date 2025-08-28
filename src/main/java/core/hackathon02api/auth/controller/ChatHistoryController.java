package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.ChatMessageDto;
import core.hackathon02api.auth.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.ZoneId;
import java.util.List;
import java.time.ZoneOffset;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatrooms")
public class ChatHistoryController {
    private final ChatMessageService chatMessageService;

    @GetMapping("/{roomId}/messages")
    public List<ChatMessageDto> getMessages(@PathVariable Long roomId) {
        return chatMessageService.findRecentMessages(roomId, 50)
                .stream()
                .map(m -> {
                    // m.getCreatedAt() 가 Instant/OffsetDateTime/LocalDateTime 중 무엇인지에 따라 분기
                    // (A) 이미 OffsetDateTime 이면 그대로 사용하면 오프셋이 보존됨
                    // OffsetDateTime created = m.getCreatedAt();

                    // (B) Instant 또는 UTC 로 저장돼 있고, 항상 KST(+09:00)로 내려주고 싶다면:
                    var created = m.getCreatedAt()              // OffsetDateTime
                            .withOffsetSameInstant(ZoneOffset.ofHours(9)); // +09:00(KST)

                    return ChatMessageDto.builder()
                            .messageId(m.getId())
                            .roomId(m.getRoom().getId())
                            .senderId(m.getSender().getId())
                            .senderNickname(m.getSender().getNickname())
                            .content(m.getContent())
                            .createdAt(created)                // ★ 더 이상 toLocalDateTime() 쓰지 않기
                            .build();
                })
                .toList();
    }
}
