package core.hackathon02api.auth.service;

import core.hackathon02api.auth.entity.*;
import core.hackathon02api.auth.repository.PostApplicationRepository;
import core.hackathon02api.auth.repository.PostRepository;
import core.hackathon02api.auth.repository.UserRepository;
import core.hackathon02api.auth.dto.ChatRoomEnterResponse;
import core.hackathon02api.auth.repository.ChatMemberRepository;
import core.hackathon02api.auth.repository.ChatMessageRepository;
import core.hackathon02api.auth.repository.ChatRoomRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import core.hackathon02api.auth.entity.ChatRoom;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMemberRepository chatMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostApplicationRepository postApplicationRepository;

    // 환경설정 또는 상수로 주입 가능
    private static final String WS_ENDPOINT = "ws://{host}/ws";

    /**
     * 채팅방 입장(없으면 생성). 호스트 또는 승인된 신청자(APPROVED/ JOINED)만 허용.
     * - 최초 생성자는 호스트일 수도/승인자일 수도 있음(현재 정책은 누구든 가능)
     * - 생성 시 호스트 멤버(HOST) 자동 등록 보장
     * - 입장자는 멤버십 없으면 MEMBER 또는 HOST로 자동 등록
     */
    public Result enter(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "user not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

        boolean isHost = post.getAuthor().getId().equals(userId);

        // 신청=자동승인 정책에 맞게 APPROVED/JOINED만 체크
        boolean approvedOrJoined = postApplicationRepository.existsByPost_IdAndApplicant_IdAndStatusIn(
                postId,
                userId,
                java.util.List.of(ApplicationStatus.APPROVED, ApplicationStatus.JOINED)
        );

        if (!isHost && !approvedOrJoined) {
            // 승인/참여 권한 없음
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "no permission to enter chatroom");
        }

        // 1) 채팅방 조회(없으면 생성)
        ChatRoom room = chatRoomRepository.findByPost_Id(postId).orElse(null);
        boolean created = false;

        if (room == null) {
            // 동시 생성 대비: post_id unique + save try-catch 후 재조회
            ChatRoom toCreate = ChatRoom.builder()
                    .post(post)
                    .hostId(post.getAuthor().getId())
                    .build();
            try {
                room = chatRoomRepository.save(toCreate);
                created = true;

                // 방 생성 시 호스트 멤버 자동 생성 보장
                if (!chatMemberRepository.existsByRoom_IdAndUser_Id(room.getId(), post.getAuthor().getId())) {
                    chatMemberRepository.save(
                            ChatMember.builder()
                                    .room(room)
                                    .user(post.getAuthor())
                                    .role(ChatMember.Role.HOST)
                                    .build()
                    );
                }
            } catch (DataIntegrityViolationException e) {
                // 경쟁 상황에서 다른 트랜잭션이 먼저 생성한 경우
                room = chatRoomRepository.findByPost_Id(postId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.INTERNAL_SERVER_ERROR, "chatroom race error"));
            }
        }

        // 2) 입장 사용자 멤버십 보장(없으면 생성)
        ChatMember.Role role = isHost ? ChatMember.Role.HOST : ChatMember.Role.MEMBER;

        ChatRoom finalRoom = room;
        ChatMember member = chatMemberRepository.findByRoom_IdAndUser_Id(room.getId(), user.getId())
                .orElseGet(() -> chatMemberRepository.save(
                        ChatMember.builder()
                                .room(finalRoom)
                                .user(user)
                                .role(role)
                                .build()
                ));

        // 3) 마지막 메시지/미읽음 계산
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

        ChatRoomEnterResponse dto = ChatRoomEnterResponse.builder()
                .roomId(room.getId())
                .postId(post.getId())
                .postTitle(post.getTitle())
                .hostId(room.getHostId())
                .joined(true)
                .role(member.getRole().name())
                .createdAt(room.getCreatedAt())
                .ws(ChatRoomEnterResponse.WsInfo.builder()
                        .endpoint(WS_ENDPOINT)
                        .subscribe("/sub/chatrooms/" + room.getId())
                        .publish("/pub/chatrooms/" + room.getId() + "/send")
                        .build())
                .lastMessage(lastDto)
                .unreadCount(unread)
                .build();

        return new Result(dto, created);
    }

    @Getter
    @RequiredArgsConstructor
    public static class Result {
        private final ChatRoomEnterResponse response;
        private final boolean created;
    }
}