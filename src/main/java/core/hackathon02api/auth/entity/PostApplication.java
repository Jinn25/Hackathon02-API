package core.hackathon02api.auth.entity;

import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.entity.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "post_application",
        uniqueConstraints = @UniqueConstraint(name = "uk_post_applicant", columnNames = {"post_id", "applicant_id"})
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
public class PostApplication {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id")
    private User applicant;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}