package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.ChatRoomEnterResponse;
import core.hackathon02api.auth.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import java.util.Map;

@RestController
@RequestMapping("/api/posts/{postId}/chatroom")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    public ResponseEntity<ChatRoomEnterResponse> createOrEnter(
            @PathVariable Long postId,
            Authentication authentication
    ) {
        if (authentication == null || authentication.getPrincipal() == null) {
            // 필터가 인증을 못 올렸을 때 안전하게 401
            return ResponseEntity.status(401).build();
        }

        // 커스텀 필터에서 넣은 principal 이 문자열(sub/username)이라고 가정
        String principal = (String) authentication.getPrincipal();

        // sub가 숫자 문자열이라면 그대로 Long 변환, 아니면 사용자 조회 로직으로 변경
        Long userId = Long.valueOf(principal);

        var result = chatRoomService.enter(userId, postId);
        return result.isCreated()
                ? ResponseEntity.status(201).body(result.getResponse())
                : ResponseEntity.ok(result.getResponse());
    }

    @PatchMapping("/{roomId}/read-cursor")
    public ResponseEntity<Void> patchCursor(
            @AuthenticationPrincipal UserDetails user,
            @PathVariable Long roomId,
            @RequestBody Map<String, Long> body
    ) {
        Long lastReadMessageId = body.get("lastReadMessageId");
        chatRoomService.updateReadCursor(
                Long.valueOf(user.getUsername()), // username을 userId로 쓰고 있다면
                roomId,
                lastReadMessageId
        );
        return ResponseEntity.noContent().build();
    }


}