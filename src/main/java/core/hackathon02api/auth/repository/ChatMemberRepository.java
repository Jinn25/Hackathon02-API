package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.ChatMember;
import core.hackathon02api.auth.entity.ChatRoom;
import core.hackathon02api.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    Optional<ChatMember> findByRoom_IdAndUser_Id(Long roomId, Long userId);
    boolean existsByRoom_IdAndUser_Id(Long roomId, Long userId);

    // ✅ 페이징 없이 전부
    List<ChatMember> findByUser_Id(Long userId);

    @Modifying
    @Query("""
    update ChatMember m
    set m.lastReadMessageId = :newCursor
    where m.room.id = :roomId and m.user.id = :userId
      and (m.lastReadMessageId is null or m.lastReadMessageId < :newCursor)
    """)
    int advanceReadCursor(@Param("roomId") Long roomId,
                          @Param("userId") Long userId,
                          @Param("newCursor") Long newCursor);
}