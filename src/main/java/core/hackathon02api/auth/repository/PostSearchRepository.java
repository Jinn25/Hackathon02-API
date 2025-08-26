package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.Post;

import java.util.List;

public interface PostSearchRepository {

    // 제목, 상품명 부분문자열 검색
    List<Post> searchByTitleOrProductNameTokens(
            String q, Long lastId, int limit,
            String category, String status, Long minPrice, Long maxPrice, Long authorId
    );

    // 카테고리 검색
    List<Post> searchByCategories(
            List<String> categories, Long lastId, int limit,
            String status, Long minPrice, Long maxPrice, Long authorId
    );
}
