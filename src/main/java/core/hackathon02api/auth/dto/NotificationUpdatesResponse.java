package core.hackathon02api.auth.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.List;

@Getter
@Builder
public class NotificationUpdatesResponse {
    private List<NotificationItemResponse> data;
    private long totalUnread;
    private Instant serverTime;
}
