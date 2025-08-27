package core.hackathon02api.auth.dto;

import core.hackathon02api.auth.entity.Notification;

import java.time.Instant;
import java.util.List;

public record NotificationItemResponse(
        Long id,
        String type,                 // 알림 타입 (예: POST_APPLIED)
        String title,
        String message,
        Long refPostId,              // 관련 게시글 ID (없을 수 있음)

        String postTitle,            // 게시글 제목
        Integer currentMemberCount,  // 신청 인원(작성자 + 승인 인원)
        Integer desiredMemberCount,  // 모집 인원
        String productDesc,                  // 가격 (없으면 null)
        String imageUrl,             // 대표 이미지 1장 (없으면 null)

        boolean isRead,
        Instant createdAt
) {
    public static NotificationItemResponse from(
            Notification n,
            String postTitle,            // 게시글 제목
            Integer currentMemberCount,  // 신청 인원(작성자 + 승인)
            Integer desiredMemberCount,  // 모집 인원
            String productDesc,                  // 가격(KRW)
            String imageUrl              // 대표 이미지
    ) {
        return new NotificationItemResponse(
                n.getId(),
                n.getType().name(),      // type 추가
                n.getTitle(),
                n.getMessage(),
                n.getRefPostId(),

                postTitle,
                currentMemberCount,
                desiredMemberCount,
                productDesc,
                imageUrl,

                n.isRead(),
                n.getCreatedAt()
        );
    }

    public record NotificationListResponse(
            List<NotificationItemResponse> items
    ) {}
}