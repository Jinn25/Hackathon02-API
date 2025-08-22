package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.PostCreateRequest;
import core.hackathon02api.auth.dto.PostResponse;
import core.hackathon02api.auth.dto.PostUpdateRequest;
import core.hackathon02api.auth.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    // 생성
    @PostMapping
    public PostResponse create(Authentication auth, @Valid @RequestBody PostCreateRequest req) {
        Long authorId = Long.valueOf((String) auth.getPrincipal()); // JwtAuthenticationFilter에서 userId 문자열로 설정
        return postService.create(authorId, req);
    }

    // 단건 조회
    @GetMapping("/{id}")
    public PostResponse get(@PathVariable Long id) {
        return postService.get(id);
    }

    // 페이지 목록
    @GetMapping
    public Page<PostResponse> list(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        return postService.list(PageRequest.of(page, size));
    }

    // 수정 (부분 업데이트)
    @PatchMapping("/{id}")
    public PostResponse update(Authentication auth, @PathVariable Long id,
                               @RequestBody PostUpdateRequest req) {
        Long requesterId = Long.valueOf((String) auth.getPrincipal());
        return postService.update(id, requesterId, req);
    }

    // 삭제 (소프트)
    @DeleteMapping("/{id}")
    public void delete(Authentication auth, @PathVariable Long id) {
        Long requesterId = Long.valueOf((String) auth.getPrincipal());
        postService.softDelete(id, requesterId);
    }
}