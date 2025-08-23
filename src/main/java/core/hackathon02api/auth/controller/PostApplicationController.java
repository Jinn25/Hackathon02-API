package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.PostApplicationResponse;
import core.hackathon02api.auth.service.PostApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/applications")
@RequiredArgsConstructor
public class PostApplicationController {

    private final PostApplicationService postApplicationService;

    // 신청
    @PostMapping
    public PostApplicationResponse apply(Authentication auth, @PathVariable Long postId) {
        Long userId = Long.valueOf((String) auth.getPrincipal());
        return postApplicationService.apply(postId, userId);
    }

    // 신청 취소
    @DeleteMapping
    public PostApplicationResponse cancel(Authentication auth, @PathVariable Long postId) {
        Long userId = Long.valueOf((String) auth.getPrincipal());
        return postApplicationService.cancel(postId, userId);
    }

    // 현재 신청 인원 수 조회 (프엔 전역 캐시 등에서 사용)
    @GetMapping("/count")
    public long count(@PathVariable Long postId) {
        return postApplicationService.count(postId);
    }
}
