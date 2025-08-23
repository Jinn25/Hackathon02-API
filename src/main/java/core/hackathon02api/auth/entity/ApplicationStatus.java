package core.hackathon02api.auth.entity;

public enum ApplicationStatus {
    APPLIED,     // 신청
    APPROVED,    // (확정 로직 도입 시)
    REJECTED,    // (거절 로직 도입 시)
    REMOVED      // 신청 취소(사용자/관리자)
}