package core.hackathon02api.auth.dto;

import core.hackathon02api.auth.entity.ApplicationStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PostApplicationResponse {
    private Long applicationId;
    private Long postId;
    private Long applicantId;
    private ApplicationStatus status;
    private long currentMemberCount;  // 현재 신청 인원
    private int desiredMemberCount;   // 목표 인원
    private String postStatus;        // Post 상태 문자열
}