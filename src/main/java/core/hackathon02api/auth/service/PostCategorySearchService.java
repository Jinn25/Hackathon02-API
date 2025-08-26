package core.hackathon02api.auth.service;

import core.hackathon02api.auth.dto.PostSearchItemResponse;
import core.hackathon02api.auth.dto.PostSearchListResponse;
import core.hackathon02api.auth.entity.ApplicationStatus;
import core.hackathon02api.auth.entity.Post;
import core.hackathon02api.auth.repository.PostApplicationRepository;
import core.hackathon02api.auth.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostCategorySearchService {

    private static final List<ApplicationStatus> COUNT_STATUSES =
            List.of(ApplicationStatus.APPROVED, ApplicationStatus.JOINED);

    private final PostRepository postRepository;
    private final PostApplicationRepository postApplicationRepository;

    public PostSearchListResponse searchByCategories(
            List<String> categories, Long lastId, Integer limit,
            String status, Long minPrice, Long maxPrice, Long authorId
    ) {
        if (categories == null || categories.isEmpty()) {
            throw new IllegalArgumentException("category 파라미터는 최소 1개 이상이어야 합니다.");
        }
        // 필요시 트림/대문자 정규화
        List<String> normalized = categories.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("category 값이 비어 있습니다.");
        }

        int size = (limit == null ? 20 : Math.min(Math.max(1, limit), 100));

        List<Post> posts = postRepository.searchByCategories(
                normalized, lastId, size, status, minPrice, maxPrice, authorId
        );

        // currentMemberCount: 승인 인원 group by 후 +1(작성자)
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Integer> currentByPost = postIds.isEmpty() ? Map.of() :
                postApplicationRepository.countApprovedByPostIds(postIds, COUNT_STATUSES).stream()
                        .collect(Collectors.toMap(
                                PostApplicationRepository.PostCountProjection::getPostId,
                                prj -> (int) (prj.getCnt() + 1)
                        ));

        // price/createdAt 안전 추출
        var items = posts.stream().map(p -> {
            Long price = extractPriceSafely(p);
            String imageUrl = resolveImageUrl(p);
            Integer current = currentByPost.getOrDefault(p.getId(), 1);
            Integer desired = Optional.ofNullable(p.getDesiredMemberCount()).orElse(0);
            String createdAt = toIsoStringSafely(p);

            return new PostSearchItemResponse(
                    p.getId(),
                    p.getTitle(),
                    p.getCategory(),
                    p.getProductName(),
                    p.getProductDesc(),
                    imageUrl,
                    current,
                    desired,
                    String.valueOf(p.getStatus()),
                    createdAt
            );
        }).toList();

        return PostSearchListResponse.of(items);
    }

    private Long extractPriceSafely(Post p) {
        try {
            Method m = p.getClass().getMethod("getPrice");
            Object v = m.invoke(p);
            if (v instanceof Number num) return num.longValue();
        } catch (NoSuchMethodException e) {
            // price 없음
        } catch (Exception ignore) {}
        return null;
    }

    private String toIsoStringSafely(Post p) {
        try {
            Object c = p.getCreatedAt();
            if (c == null) return null;
            if (c instanceof Instant i) return i.toString();
            if (c instanceof OffsetDateTime odt) return odt.toInstant().toString();
            if (c instanceof LocalDateTime ldt) return ldt.atZone(ZoneId.systemDefault()).toInstant().toString();
            return c.toString();
        } catch (Exception ignore) { return null; }
    }

    private String resolveImageUrl(Post p) {
        if (p.getMainImageUrl() != null && !p.getMainImageUrl().isBlank()) return p.getMainImageUrl();
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) return p.getImageUrls().get(0);
        return null;
    }
}
