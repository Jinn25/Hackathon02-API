package core.hackathon02api.auth.dto;

import core.hackathon02api.auth.entity.ApplicationStatus;
import core.hackathon02api.auth.entity.PostStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MyPagePostCard {
    private Long postId;
    private String title;
    private PostStatus postStatus;

    // 인원
    private int currentMemberCount;   // (승인/참여) + 작성자 1
    private Integer desiredMemberCount;

    // 신청자 관점일 때만 채움
    private Long myApplicationId;
    private ApplicationStatus myApplicationStatus;
    private LocalDateTime appliedAt;
}