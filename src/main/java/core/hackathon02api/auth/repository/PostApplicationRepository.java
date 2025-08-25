package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.PostApplication;
import core.hackathon02api.auth.entity.ApplicationStatus; // ← enum 경로에 맞게
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostApplicationRepository extends JpaRepository<PostApplication, Long> {

    // ── 기존 메서드(유지) ─────────────────────────────────────────────
    long countByPost_Id(Long postId);
    boolean existsByPost_IdAndApplicant_Id(Long postId, Long applicantId);
    Optional<PostApplication> findByPost_IdAndApplicant_Id(Long postId, Long applicantId);

    // ── 신규: 승인 상태만 카운트 (현재 인원 계산용) ───────────────────
    long countByPost_IdAndStatusIn(Long postId, Collection<ApplicationStatus> statuses);

    // ── (옵션) 목록 최적화: 여러 postId를 한 번에 group by로 카운트 ──
    @Query("""
        select pa.post.id as postId, count(pa.id) as cnt
        from PostApplication pa
        where pa.post.id in :postIds and pa.status in :statuses
        group by pa.post.id
    """)
    List<PostCountProjection> countByPostIdsAndStatusIn(@Param("postIds") Collection<Long> postIds,
                                                        @Param("statuses") Collection<ApplicationStatus> statuses);

    interface PostCountProjection {
        Long getPostId();
        long getCnt();
    }

    boolean existsByPost_IdAndApplicant_IdAndStatusIn(
            Long postId,
            Long applicantId,
            List<ApplicationStatus> statuses
    );

}
