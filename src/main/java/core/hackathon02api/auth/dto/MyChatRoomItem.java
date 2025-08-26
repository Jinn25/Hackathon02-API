package core.hackathon02api.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;

@Getter
@Builder
public class MyChatRoomItem {
    private Long roomId;

    private Long postId;
    private String postTitle;
    private String postMainImageUrl; // Post에 있다면 내려줌 (선택)
    private Long hostId;

    private String role; // "HOST" | "MEMBER"

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private LastMessage lastMessage;

    private long unreadCount;

    private OffsetDateTime createdAt;      // 방 생성 시각
    private OffsetDateTime lastActivityAt; // 마지막 메시지 시각(없으면 createdAt)

    private WsInfo ws; // 바로 구독/발행 경로

    @Getter @Builder
    public static class LastMessage {
        private Long messageId;
        private Long senderId;
        private String content;
        private OffsetDateTime createdAt;
    }

    @Getter @Builder
    public static class WsInfo {
        private String endpoint;
        private String subscribe;
        private String publish;
    }
}