package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.ChatMember;
import core.hackathon02api.auth.entity.ChatRoom;
import core.hackathon02api.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    Optional<ChatMember> findByRoom_IdAndUser_Id(Long roomId, Long userId);
    boolean existsByRoom_IdAndUser_Id(Long roomId, Long userId);
}