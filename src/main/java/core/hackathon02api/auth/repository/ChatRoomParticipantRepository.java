package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.ChatRoomParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomParticipantRepository extends JpaRepository<ChatRoomParticipant, Long> {
    Optional<ChatRoomParticipant> findByChatRoom_IdAndUser_Id(Long chatRoomId, Long userId);
}