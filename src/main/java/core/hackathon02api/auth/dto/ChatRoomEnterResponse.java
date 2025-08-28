package core.hackathon02api.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.OffsetDateTime;

@Getter @Builder
public class ChatRoomEnterResponse {
    private Long roomId;
    private Long postId;
    private String postTitle;
    private Long hostId;
    private boolean joined;
    private String role; // "HOST" | "MEMBER"
    private OffsetDateTime createdAt;
    private WsInfo ws;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LastMessage lastMessage;
    private long unreadCount;
    private Integer desiredMemberCount;
    private String productDesc;
    private String mainImageUrl;

    @Getter @Builder
    public static class WsInfo {
        private String endpoint;
        private String subscribe;
        private String publish;
    }

    @Getter @Builder
    public static class LastMessage {
        private Long messageId;
        private Long senderId;
        private String content;
        private OffsetDateTime createdAt;
    }
}