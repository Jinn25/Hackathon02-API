package core.hackathon02api.auth.service;

import core.hackathon02api.auth.dto.ChatRoomEnterResponse;
import core.hackathon02api.auth.dto.MyChatRoomItem;
import core.hackathon02api.auth.entity.ChatMember;
import core.hackathon02api.auth.entity.ChatMessage;
import core.hackathon02api.auth.entity.ChatRoom;
import core.hackathon02api.auth.entity.Post;
import core.hackathon02api.auth.repository.ChatMemberRepository;
import core.hackathon02api.auth.repository.ChatMessageRepository;
import core.hackathon02api.auth.repository.ChatRoomRepository;
import core.hackathon02api.auth.repository.PostRepository;
import core.hackathon02api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyChatRoomService {

    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private static final String WS_ENDPOINT = "ws://{host}/ws";

    public List<MyChatRoomItem> listMyRooms(Long userId) {
        userRepository.findById(userId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found")
        );

        List<ChatMember> memberships = chatMemberRepository.findByUser_Id(userId);

        List<MyChatRoomItem> items = memberships.stream().map(m -> {
                    ChatRoom room = m.getRoom();
                    Post post = room.getPost();

                    ChatMessage last = chatMessageRepository.findTopByRoom_IdOrderByIdDesc(room.getId()).orElse(null);

                    long unread;
                    if (m.getLastReadMessageId() == null) {
                        unread = (last == null) ? 0 : chatMessageRepository.countByRoom_Id(room.getId());
                    } else {
                        unread = chatMessageRepository.countByRoom_IdAndIdGreaterThan(
                                room.getId(), m.getLastReadMessageId());
                    }

                    MyChatRoomItem.LastMessage lastDto = null;
                    OffsetDateTime lastActivityAt = room.getCreatedAt();
                    if (last != null) {
                        lastActivityAt = last.getCreatedAt();
                        lastDto = MyChatRoomItem.LastMessage.builder()
                                .messageId(last.getId())
                                .senderId(last.getSender().getId())
                                .content(last.getContent())
                                .createdAt(last.getCreatedAt())
                                .build();
                    }

                    return MyChatRoomItem.builder()
                            .roomId(room.getId())
                            .postId(post.getId())
                            .postTitle(post.getTitle())
                            .postMainImageUrl(post.getMainImageUrl()) // Post에 필드 있으면
                            .hostId(room.getHostId())
                            .role(m.getRole().name())
                            .lastMessage(lastDto)
                            .unreadCount(unread)
                            .createdAt(room.getCreatedAt())
                            .lastActivityAt(lastActivityAt)
                            .ws(MyChatRoomItem.WsInfo.builder()
                                    .endpoint(WS_ENDPOINT)
                                    .subscribe("/sub/chatrooms/" + room.getId())
                                    .publish("/pub/chatrooms/" + room.getId() + "/send")
                                    .build())
                            .build();
                }).sorted(Comparator.comparing(MyChatRoomItem::getLastActivityAt).reversed())
                .toList();

        return items;
    }

    @Transactional
    public ChatRoomEnterResponse open(Long userId, Long roomId) {
        ChatMember member = chatMemberRepository.findByRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "not a member of room"));

        ChatRoom room = member.getRoom();
        Post post = room.getPost();

        ChatMessage last = chatMessageRepository.findTopByRoom_IdOrderByIdDesc(room.getId()).orElse(null);

        long unread;
        if (member.getLastReadMessageId() == null) {
            unread = (last == null) ? 0 : chatMessageRepository.countByRoom_Id(room.getId());
        } else {
            unread = chatMessageRepository.countByRoom_IdAndIdGreaterThan(
                    room.getId(), member.getLastReadMessageId());
        }

        ChatRoomEnterResponse.LastMessage lastDto = null;
        if (last != null) {
            lastDto = ChatRoomEnterResponse.LastMessage.builder()
                    .messageId(last.getId())
                    .senderId(last.getSender().getId())
                    .content(last.getContent())
                    .createdAt(last.getCreatedAt())
                    .build();
        }

        return ChatRoomEnterResponse.builder()
                .roomId(room.getId())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .hostId(room.getHostId())
                .joined(true)
                .role(member.getRole().name())
                .createdAt(room.getCreatedAt())
                .ws(ChatRoomEnterResponse.WsInfo.builder()
                        .endpoint("ws://{host}/ws")
                        .subscribe("/sub/chatrooms/" + room.getId())
                        .publish("/pub/chatrooms/" + room.getId() + "/send")
                        .build())
                .lastMessage(lastDto)
                .unreadCount(unread)
                .build();
    }

    @Transactional
    public void markRead(Long userId, Long roomId, Long lastSeenMessageId) {
        ChatMember member = chatMemberRepository.findByRoom_IdAndUser_Id(roomId, userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "not a member of room"));

        if (lastSeenMessageId != null &&
                (member.getLastReadMessageId() == null || lastSeenMessageId > member.getLastReadMessageId())) {
            member.setLastReadMessageId(lastSeenMessageId); // Lombok @Setter 없으면 setter 추가 or 엔티티에 markRead 메서드 만들어서 사용
        }
        // JPA dirty checking으로 업데이트됨
    }
}