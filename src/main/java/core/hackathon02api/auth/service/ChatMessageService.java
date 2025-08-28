package core.hackathon02api.auth.service;

import core.hackathon02api.auth.dto.ChatMessageDto;
import core.hackathon02api.auth.entity.ChatMessage;
import core.hackathon02api.auth.entity.ChatRoom;
import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.repository.ChatMemberRepository;
import core.hackathon02api.auth.repository.ChatMessageRepository;
import core.hackathon02api.auth.repository.ChatRoomRepository;
import core.hackathon02api.auth.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
// 추가 (필요 시)
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
@Service
@RequiredArgsConstructor
public class ChatMessageService {
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMemberRepository chatMemberRepository;

    @Transactional
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

        // ✅ 2-1) 내가 보낸 메시지는 자동으로 읽음 처리

        chatMemberRepository.findByRoom_IdAndUser_Id(roomId, sender.getId())
                .ifPresent(member -> {
                    // 뒤로 가지 않도록 보장
                    if (member.getLastReadMessageId() == null || member.getLastReadMessageId() < saved.getId()) {
                        member.setLastReadMessageId(saved.getId());
                        chatMemberRepository.save(member);
                    }
                });

        // ✅ createdAt 을 OffsetDateTime 그대로 사용 (필요하면 +09:00 로 보정)
        OffsetDateTime created = saved.getCreatedAt() != null
                ? saved.getCreatedAt().withOffsetSameInstant(ZoneOffset.ofHours(9)) // 클라가 KST 원하면 유지
                : OffsetDateTime.now(ZoneOffset.ofHours(9));

        ChatMessageDto out = ChatMessageDto.builder()
                .messageId(saved.getId())
                .roomId(roomId)
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
                .content(saved.getContent())
                .createdAt(created) // << 절대 toLocalDateTime() 쓰지 않기
                .build();

        System.out.println("createdAt: " + saved.getCreatedAt());
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
        return chatMessageRepository.findByRoom_IdOrderByCreatedAtDesc(roomId, PageRequest.of(0, limit))
                .stream()
                .map(m -> {
                    OffsetDateTime created = m.getCreatedAt() != null
                            ? m.getCreatedAt().withOffsetSameInstant(ZoneOffset.ofHours(9))
                            : OffsetDateTime.now(ZoneOffset.ofHours(9));
                    return ChatMessageDto.builder()
                            .messageId(m.getId())
                            .roomId(m.getRoom() != null ? m.getRoom().getId() : null)
                            .senderId(m.getSender() != null ? m.getSender().getId() : null)
                            .senderNickname(m.getSender() != null ? m.getSender().getNickname() : "알 수 없음")
                            .content(m.getContent())
                            .createdAt(created)
                            .build();
                })
                // 오래된 것 → 최신 (리포지토리에서 이미 desc 이면 이 정렬은 선택)
                .sorted(Comparator.comparing(ChatMessageDto::getCreatedAt))
                .toList();
    }
}
