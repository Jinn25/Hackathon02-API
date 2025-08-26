package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.PostSearchListResponse;
import core.hackathon02api.auth.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/posts/search")
@RequiredArgsConstructor
public class PostSearchController {

    private final PostSearchService postSearchService;

    @GetMapping
    public PostSearchListResponse search(
            @RequestParam String q,
            @RequestParam(required = false) Long lastId,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String category,
            @RequestParam(required = false, defaultValue = "OPEN") String status, // ALL이면 상태 미필터
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Long authorId
    ) {
        try {
            return postSearchService.search(q, lastId, limit, category, status, minPrice, maxPrice, authorId);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
