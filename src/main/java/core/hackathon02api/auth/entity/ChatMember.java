package core.hackathon02api.auth.entity;

import core.hackathon02api.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity @Table(name="chat_member", uniqueConstraints = {
        @UniqueConstraint(name="uk_room_user", columnNames = {"room_id","user_id"})
})
@Getter @Builder @NoArgsConstructor @AllArgsConstructor @Setter
public class ChatMember {

    public enum Role { HOST, MEMBER }

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="room_id", nullable=false)
    private ChatRoom room;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="user_id", nullable=false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name="role", length=16, nullable=false)
    private Role role;

    // 마지막으로 읽은 메시지 ID (없으면 null)
    @Column(name="last_read_message_id")
    private Long lastReadMessageId;

    @CreationTimestamp
    @Column(name="joined_at", nullable=false)
    private OffsetDateTime joinedAt;
}