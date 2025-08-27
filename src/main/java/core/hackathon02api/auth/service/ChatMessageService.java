package core.hackathon02api.auth.service;

import core.hackathon02api.auth.dto.ChatMessageDto;
import core.hackathon02api.auth.entity.ChatMessage;
import core.hackathon02api.auth.entity.ChatRoom;
import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.repository.ChatMessageRepository;
import core.hackathon02api.auth.repository.ChatRoomRepository;
import core.hackathon02api.auth.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public void saveAndBroadcast(Long roomId, ChatMessageDto dto) {
        // 1) 엔티티 조회
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("보낸 사용자가 존재하지 않습니다."));

        // 2) 저장
        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(dto.getContent())
                // 엔티티가 OffsetDateTime라면 여기서 now()로 세팅하거나 @PrePersist 사용
                .build();
        ChatMessage saved = chatMessageRepository.save(message);

        // 3) 브로드캐스트 (빌더 사용)
        ChatMessageDto out = ChatMessageDto.builder()
                .messageId(saved.getId()) // ★ 프론트 중복제거/키용
                .roomId(roomId)
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt().toLocalDateTime())
                .build();

        messagingTemplate.convertAndSend("/sub/chatrooms/" + roomId, out);
        System.out.println("브로드캐스트 실행됨: " + out.getContent());
    }

    @Transactional
    public List<ChatMessage> findRecentMessages(Long roomId, int limit) {
        return chatMessageRepository.findByRoom_IdOrderByCreatedAtDesc(
                roomId,
                PageRequest.of(0, limit)
        );
    }

    @Transactional
    public List<ChatMessageDto> findRecentMessagesDto(Long roomId, int limit) {
        return chatMessageRepository.findByRoom_IdOrderByCreatedAtDesc(
                        roomId,
                        PageRequest.of(0, limit)
                ).stream()
                .map(m -> ChatMessageDto.builder()
                        .messageId(m.getId()) // ★ 추가
                        .roomId(m.getRoom().getId())
                        .senderId(m.getSender().getId())
                        .senderNickname(m.getSender().getNickname())
                        .content(m.getContent())
                        .createdAt(m.getCreatedAt().toLocalDateTime())
                        .build()
                )
                // 오래된 것 → 최신
                .sorted(Comparator.comparing(ChatMessageDto::getCreatedAt))
                .toList();
    }
}
