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

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;

    private final SimpMessagingTemplate messagingTemplate;

    public void saveAndBroadcast(Long roomId, ChatMessageDto dto) {
        // 1. 엔티티 조회
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("채팅방이 존재하지 않습니다."));
        User sender = userRepository.findById(dto.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("보낸 사용자가 존재하지 않습니다."));

        // 2. DB 저장
        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .content(dto.getContent())
                .createdAt(OffsetDateTime.now())
                .build();
        chatMessageRepository.save(message);

        // 3. 구독자에게 브로드캐스트
        messagingTemplate.convertAndSend(
                "/sub/chatrooms/" + roomId,
                new ChatMessageDto(
                        roomId,
                        sender.getId(),
                        message.getContent(),
                        message.getCreatedAt().toLocalDateTime()
                )

        );
        System.out.println("브로드캐스트 실행됨: " + dto.getContent());
    }

    @Transactional
    public List<ChatMessage> findRecentMessages(Long roomId, int limit) {
        return chatMessageRepository.findByRoom_IdOrderByCreatedAtDesc(
                roomId,
                PageRequest.of(0, limit) // limit 개수만큼
        );
    }
}
