package core.hackathon02api.auth.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MarkReadRequest {
    // 클라이언트가 "이 메시지 ID까지 읽었다" 알려주면 그 지점까지 읽음 처리
    private Long lastSeenMessageId;
}