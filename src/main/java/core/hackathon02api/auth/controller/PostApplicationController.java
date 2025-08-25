package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.PostApplicationResponse;
import core.hackathon02api.auth.service.PostApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/posts/{postId}/applications")
@RequiredArgsConstructor
public class PostApplicationController {

    private final PostApplicationService postApplicationService;

    @PostMapping
    public ResponseEntity<PostApplicationResponse> apply(
            @PathVariable Long postId,
            Authentication auth
    ) {
        if (auth == null || auth.getPrincipal() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Object principal = auth.getPrincipal();
        Long userId;
        if (principal instanceof String s) {           // 현재 환경: principal == "2" 같은 문자열
            try {
                userId = Long.valueOf(s);
            } catch (NumberFormatException e) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } else {
            // CustomUserDetails를 쓰는 경우에 대비
            // userId = ((CustomUserDetails) principal).getId();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        PostApplicationResponse body = postApplicationService.apply(postId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/count")
    public int count(@PathVariable Long postId) {
        return postApplicationService.countCurrentMembers(postId); // 작성자+승인 포함 계산으로 맞춰둔 메서드
    }
}
