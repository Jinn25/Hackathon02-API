package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    Optional<ChatMessage> findTopByRoom_IdOrderByIdDesc(Long roomId);
    long countByRoom_IdAndIdGreaterThan(Long roomId, Long lastReadMessageId);
    long countByRoom_Id(Long roomId); // lastReadMessageId == null 대비

    List<ChatMessage> findByRoomId(Long roomId, Pageable pageable);

    List<ChatMessage> findByRoom_IdOrderByCreatedAtDesc(Long roomId, Pageable pageable);

    List<ChatMessage> findTop50ByRoomIdOrderByCreatedAtDesc(Long roomId);
}
