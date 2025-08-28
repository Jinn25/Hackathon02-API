package core.hackathon02api.auth.entity;

import core.hackathon02api.auth.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;

@Entity @Table(name="chat_message", indexes = {
        @Index(name="ix_room_id_id", columnList = "room_id,id")
})
@Getter @Builder @NoArgsConstructor @AllArgsConstructor
public class ChatMessage {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="room_id", nullable=false)
    private ChatRoom room;

    @ManyToOne(fetch=FetchType.LAZY, optional=false)
    @JoinColumn(name="sender_id", nullable=false)
    private User sender;

    @Column(name="content", nullable=false, length=2000)
    private String content;

    @CreationTimestamp
    @Column(name="created_at", nullable=false)
    private OffsetDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = OffsetDateTime.now();
        }
    }


}

/**
 * 타임스탬프는 OffsetDateTime으로 저장 → 응답이 +09:00 형식으로 직렬화되도록
 * Jackson 기본 WRITE_DATES_WITH_ZONE_ID 세팅 또는 spring.jackson.time-zone=Asia/Seoul을
 * application.yml에 설정 권장.
 * */