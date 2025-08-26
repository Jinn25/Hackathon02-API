package core.hackathon02api.auth.dto;

import java.util.List;

public record NotificationListResponse(
        List<NotificationItemResponse> items
) {
    public static final NotificationListResponse EMPTY = new NotificationListResponse(List.of());

    public static NotificationListResponse of(List<NotificationItemResponse> items) {
        return (items == null || items.isEmpty()) ? EMPTY : new NotificationListResponse(items);
    }
}