package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.Post;
import core.hackathon02api.auth.entity.PostStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class PostSearchRepositoryImpl implements PostSearchRepository {
    @PersistenceContext
    private final EntityManager em;

    @Override
    public List<Post> searchByTitleOrProductNameTokens(
            String q, Long lastId, int limit,
            String category, String status, Long minPrice, Long maxPrice, Long authorId
    ) {
        var tokens = Arrays.stream(q.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .map(String::toLowerCase)
                .toList();

        StringBuilder jpql = new StringBuilder("""
            select p from Post p
            where 1=1
              and (:lastId is null or p.id < :lastId)
        """);

        // 상태 필터
        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            jpql.append(" and p.status = :status");
        }
        if (category != null && !category.isBlank()) {
            jpql.append(" and p.category = :category");
        }
        if (minPrice != null) jpql.append(" and p.price >= :minPrice");
        if (maxPrice != null) jpql.append(" and p.price <= :maxPrice");
        if (authorId != null) jpql.append(" and p.author.id = :authorId");

        // 토큰 AND: 각 토큰은 (title OR productName)
        for (int i = 0; i < tokens.size(); i++) {
            jpql.append("""
                and (
                    lower(p.title) like :t%1$d
                 or lower(p.productName) like :t%1$d
                )
            """.formatted(i));
        }

        jpql.append(" order by p.id desc");

        var query = em.createQuery(jpql.toString(), Post.class)
                .setParameter("lastId", lastId)
                .setMaxResults(Math.min(Math.max(1, limit), 100));

        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            query.setParameter("status", PostStatus.valueOf(status));
        }
        if (category != null && !category.isBlank()) query.setParameter("category", category);
        if (minPrice != null) query.setParameter("minPrice", minPrice);
        if (maxPrice != null) query.setParameter("maxPrice", maxPrice);
        if (authorId != null) query.setParameter("authorId", authorId);

        for (int i = 0; i < tokens.size(); i++) {
            query.setParameter("t" + i, "%" + tokens.get(i) + "%");
        }

        return query.getResultList();
    }

    @Override
    public List<Post> searchByCategories(
            List<String> categories, Long lastId, int limit,
            String status, Long minPrice, Long maxPrice, Long authorId
    ) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("category는 최소 1개 이상 필요합니다.");
        }

        StringBuilder jpql = new StringBuilder("""
            select p from Post p
            where 1=1
              and (:lastId is null or p.id < :lastId)
              and p.category in :categories
        """);

        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            jpql.append(" and p.status = :status");
        }
        if (minPrice != null) jpql.append(" and p.price >= :minPrice");
        if (maxPrice != null) jpql.append(" and p.price <= :maxPrice");
        if (authorId != null) jpql.append(" and p.author.id = :authorId");

        jpql.append(" order by p.id desc");

        var query = em.createQuery(jpql.toString(), Post.class)
                .setParameter("lastId", lastId)
                .setParameter("categories", categories)
                .setMaxResults(Math.min(Math.max(1, limit), 100));

        if (status != null && !"ALL".equalsIgnoreCase(status)) {
            query.setParameter("status", PostStatus.valueOf(status));
        }
        if (minPrice != null) query.setParameter("minPrice", minPrice);
        if (maxPrice != null) query.setParameter("maxPrice", maxPrice);
        if (authorId != null) query.setParameter("authorId", authorId);

        return query.getResultList();
    }

}
