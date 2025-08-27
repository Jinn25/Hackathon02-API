package core.hackathon02api.auth.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessageDto {
    private Long messageId;          // 추가
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String content;
    private LocalDateTime createdAt;

}
