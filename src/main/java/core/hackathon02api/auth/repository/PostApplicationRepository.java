package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.ApplicationStatus;
import core.hackathon02api.auth.entity.PostApplication;
import core.hackathon02api.auth.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostApplicationRepository extends JpaRepository<PostApplication, Long> {

    // ===== 단건/존재/카운트 =====
    long countByPost_Id(Long postId);

    boolean existsByPost_IdAndApplicant_Id(Long postId, Long applicantId);

    Optional<PostApplication> findByPost_IdAndApplicant_Id(Long postId, Long applicantId);

    long countByPost_IdAndStatusIn(Long postId, Collection<ApplicationStatus> statuses);

    // ===== group-by 집계 Projection =====
    interface PostCountProjection {
        Long getPostId();
        long getCnt();
    }

    // ===== 여러 게시글 신청 인원 집계: 상태 무관 =====
    @Query("""
        select pa.post.id as postId, count(pa.id) as cnt
        from PostApplication pa
        where pa.post.id in :postIds
        group by pa.post.id
    """)
    List<PostCountProjection> countByPostIdsGroup(@Param("postIds") Collection<Long> postIds);

    // ===== 여러 게시글 신청 인원 집계: 특정 상태들만(예: APPROVED 등) =====
    @Query("""
        select pa.post.id as postId, count(pa.id) as cnt
        from PostApplication pa
        where pa.post.id in :postIds and pa.status in :statuses
        group by pa.post.id
    """)
    List<PostCountProjection> countApprovedByPostIds(
            @Param("postIds") Collection<Long> postIds,
            @Param("statuses") Collection<ApplicationStatus> statuses
    );

    // ===== 리스트 조회(상태 필터) =====
    List<PostApplication> findAllByPost_IdAndStatusIn(Long postId, Collection<ApplicationStatus> statuses);

    boolean existsByPost_IdAndApplicant_IdAndStatusIn(
            Long postId,
            Long applicantId,
            Collection<ApplicationStatus> statuses
    );

    // ===== 마이페이지: 내가 신청한 글(내 신청 상태 기반) =====
    List<PostApplication> findAllByApplicant_IdAndStatusIn(Long userId, Collection<ApplicationStatus> statuses);

    // ===== 마이페이지: 내가 신청한 글(게시글 상태 기반) =====
    List<PostApplication> findAllByApplicant_IdAndPost_Status(Long userId, PostStatus status);

    // ===== 조합 예시: (내 상태, 게시글 상태) 한 쌍으로 조회 =====
    List<PostApplication> findAllByApplicant_IdAndStatusAndPost_Status(
            Long userId, ApplicationStatus status, PostStatus postStatus
    );
}
