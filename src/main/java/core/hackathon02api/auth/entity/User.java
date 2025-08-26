package core.hackathon02api.auth.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.*;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "users") // DB 테이블명
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // 회원 고유 ID

    @Column(nullable = false, unique = true)
    private String username; // 아이디

    @Column(nullable = false)
    private String passwordHash; // 비밀번호 (BCrypt 해시)

    @Column(nullable = false, unique = true)
    private String nickname; // 닉네임

    private String gender;     // 성별 (MALE/FEMALE/OTHER)

    private String ageRange;   // 연령대 (TEEN, 20s, 30s ...)

    private String roadAddress; // 사는 곳

    @Transient
    private List<String> interests;

    @JsonIgnore // DB에 저장된 원본 JSON은 응답에 안 보이게
    private String interestsJson;

    @PostLoad
    @PostPersist
    @PostUpdate
    private void hydrateInterests() {
        try {
            if (interestsJson == null || interestsJson.isBlank()) {
                this.interests = Collections.emptyList();
            } else {
                ObjectMapper om = new ObjectMapper();
                this.interests = om.readValue(interestsJson,
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            }
        } catch (Exception e) {
            this.interests = Collections.emptyList(); // 실패해도 빈 리스트
        }
    }
}
