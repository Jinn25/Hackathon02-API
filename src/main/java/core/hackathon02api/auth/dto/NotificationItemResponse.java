package core.hackathon02api.auth.dto;

import core.hackathon02api.auth.entity.Notification;

import java.time.Instant;

public record NotificationItemResponse(
        Long id,
        String title,
        String message,
        Long refPostId,
        boolean isRead,
        Instant createdAt
) {
    public static NotificationItemResponse from(Notification n) {
        return new NotificationItemResponse(
                n.getId(),
                n.getTitle(),
                n.getMessage(),
                n.getRefPostId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}