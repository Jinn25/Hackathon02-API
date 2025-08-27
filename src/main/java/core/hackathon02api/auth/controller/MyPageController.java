package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.MyPagePostCard;
import core.hackathon02api.auth.dto.PostResponse;
import core.hackathon02api.auth.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
public class MyPageController {

    private final MyPageService myPageService;

    private Long getUserId(Authentication auth) {
        if (auth == null || auth.getPrincipal() == null) return null;
        Object principal = auth.getPrincipal();
        if (principal instanceof String s) {
            try { return Long.valueOf(s); } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    // 신청중 = APPROVED & POST.OPEN
    @GetMapping("/applied/ongoing")
    public ResponseEntity<List<PostResponse>> appliedOngoing(Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(myPageService.getAppliedOngoing(userId));
    }

    // 완료됨 = APPROVED & POST.FULL
    @GetMapping("/applied/completed")
    public ResponseEntity<List<PostResponse>> appliedCompleted(Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(myPageService.getAppliedCompleted(userId));
    }

    @GetMapping("/my-posts")
    public ResponseEntity<List<PostResponse>> myPosts(Authentication auth) {
        Long userId = getUserId(auth);
        if (userId == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(myPageService.getMyPosts(userId));
    }
}
