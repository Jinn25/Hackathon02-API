package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.PostSearchListResponse;
import core.hackathon02api.auth.service.PostCategorySearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/posts/search/category")
@RequiredArgsConstructor
public class PostCategorySearchController {

    private final PostCategorySearchService service;

    /**
     * 카테고리 기반 게시글 검색 (무한 스크롤: lastId, limit)
     * 예) /api/posts/search/category?category=OUTDOOR&category=FOOD&limit=20&lastId=123
     */
    @GetMapping
    public PostSearchListResponse searchByCategory(
            @RequestParam List<String> category,                 // 다중 파라미터 지원
            @RequestParam(required = false) Long lastId,         // 커서 (id < lastId)
            @RequestParam(required = false) Integer limit,       // 기본 20, 1~100
            @RequestParam(required = false, defaultValue = "OPEN") String status, // ALL이면 상태 미필터
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(required = false) Long authorId
    ) {
        try {
            return service.searchByCategories(category, lastId, limit, status, minPrice, maxPrice, authorId);
        } catch (IllegalArgumentException e) {
            // 파라미터 검증 실패시 400으로 변환
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
