package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.PostCreateRequest;
import core.hackathon02api.auth.dto.PostResponse;
import core.hackathon02api.auth.dto.PostUpdateRequest;
import core.hackathon02api.auth.entity.PostStatus;
import core.hackathon02api.auth.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

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

    // 전체 목록 (Full-Fetch)
    // 필요하면 상태 필터를 쉼표로: ?statuses=OPEN,FULL
    @GetMapping
    public List<PostResponse> list(@RequestParam(required = false) String statuses) {
        List<PostStatus> statusFilters = null;
        if (statuses != null && !statuses.isBlank()) {
            statusFilters = Arrays.stream(statuses.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(PostStatus::valueOf)
                    .toList();
        }
        return postService.listAll(statusFilters);
    }

    // 부분 수정
    @PatchMapping("/{id}")
    public PostResponse update(Authentication auth, @PathVariable Long id,
                               @RequestBody PostUpdateRequest req) {
        Long requesterId = Long.valueOf((String) auth.getPrincipal());
        return postService.update(id, requesterId, req);
    }

    // 소프트 삭제
    @DeleteMapping("/{id}")
    public void delete(Authentication auth, @PathVariable Long id) {
        Long requesterId = Long.valueOf((String) auth.getPrincipal());
        postService.softDelete(id, requesterId);
    }
}
