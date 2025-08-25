package core.hackathon02api.auth.entity;

import core.hackathon02api.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity @Table(name="chat_room", uniqueConstraints = {
        @UniqueConstraint(name="uk_chat_room_post", columnNames = "post_id")
})
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatRoom {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="post_id", nullable=false)
    private Post post;

    @Column(name="host_id", nullable=false)
    private Long hostId;

    @CreationTimestamp
    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;
}