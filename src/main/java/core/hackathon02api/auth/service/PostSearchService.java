// core/hackathon02api/auth/service/PostSearchService.java
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
public class PostSearchService {

    private static final List<ApplicationStatus> COUNT_STATUSES =
            List.of(ApplicationStatus.APPROVED, ApplicationStatus.JOINED);

    private final PostRepository postRepository;
    private final PostApplicationRepository postApplicationRepository;

    public PostSearchListResponse search(
            String q, Long lastId, Integer limit,
            String category, String status, Long minPrice, Long maxPrice, Long authorId
    ) {
        if (q == null || q.trim().isBlank()) {
            throw new IllegalArgumentException("검색어(q)는 필수입니다.");
        }
        int size = (limit == null ? 20 : Math.min(Math.max(1, limit), 100));

        List<Post> posts = postRepository.searchByTitleOrProductNameTokens(
                q, lastId, size, category, status, minPrice, maxPrice, authorId
        );

        // currentMemberCount 계산: group by 후 +1(작성자)
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Integer> currentByPost = postIds.isEmpty() ? Map.of() :
                postApplicationRepository.countApprovedByPostIds(postIds, COUNT_STATUSES)
                        .stream()
                        .collect(Collectors.toMap(
                                PostApplicationRepository.PostCountProjection::getPostId,
                                prj -> (int) (prj.getCnt() + 1)
                        ));

        var items = posts.stream().map(p -> {
            Long price = extractPriceSafely(p);         // 반사 안전하게 (없으면 null)
            String imageUrl = resolveImageUrl(p);
            Integer current = currentByPost.getOrDefault(p.getId(), 1); // 최소 작성자 1
            Integer desired = Optional.ofNullable(p.getDesiredMemberCount()).orElse(0);
            String createdAt = toIsoStringSafely(p);    // 어떤 타입이든 문자열 변환

            String roadAddress = (p.getAuthor() != null) ? p.getAuthor().getRoadAddress() : null;
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
                    createdAt,
                    roadAddress
            );
        }).toList();

        return PostSearchListResponse.of(items);
    }

    /** price 게터가 있다면 읽고, 없으면 null */
    private Long extractPriceSafely(Post p) {
        try {
            // 프록시 클래스까지 고려: p.getClass()에서 메서드 탐색
            Method m = p.getClass().getMethod("getPrice");
            Object v = m.invoke(p);
            if (v instanceof Number num) return num.longValue();
        } catch (NoSuchMethodException e) {
            // price 필드/게터 없음 → null
        } catch (Exception ignore) {
            // 접근/호출 실패 → null
        }
        return null;
    }

    /** createdAt을 문자열(ISO 유사)로 안전 변환 */
    private String toIsoStringSafely(Post p) {
        try {
            Object c = p.getCreatedAt(); // 엔티티에 게터가 있다는 전제 (다른 곳에서도 사용 중)
            if (c == null) return null;

            if (c instanceof Instant i) return i.toString();
            if (c instanceof OffsetDateTime odt) return odt.toInstant().toString();
            if (c instanceof LocalDateTime ldt) {
                // 서버 기본 TZ 기준으로 Instant 변환 후 ISO 문자열
                return ldt.atZone(ZoneId.systemDefault()).toInstant().toString();
            }
            // 그 외 타입은 toString()
            return c.toString();
        } catch (Exception ignore) {
            return null;
        }
    }

    private String resolveImageUrl(Post p) {
        if (p.getMainImageUrl() != null && !p.getMainImageUrl().isBlank()) return p.getMainImageUrl();
        if (p.getImageUrls() != null && !p.getImageUrls().isEmpty()) return p.getImageUrls().get(0);
        return null;
    }
}
