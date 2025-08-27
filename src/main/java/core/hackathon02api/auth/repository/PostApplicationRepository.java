package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.PostApplication;
import core.hackathon02api.auth.entity.ApplicationStatus;
import core.hackathon02api.auth.entity.PostStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostApplicationRepository extends JpaRepository<PostApplication, Long> {

    long countByPost_Id(Long postId);

    boolean existsByPost_IdAndApplicant_Id(Long postId, Long applicantId);

    Optional<PostApplication> findByPost_IdAndApplicant_Id(Long postId, Long applicantId);

    long countByPost_IdAndStatusIn(Long postId, Collection<ApplicationStatus> statuses);

    interface PostCountProjection {
        Long getPostId();
        long getCnt();
    }

    // ✅ 문제의 메서드: 파생쿼리 금지, @Query로 명시
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

    // (원하면 유지) 동의어 메서드 — 같은 @Query 재사용 가능
    @Query("""
        select pa.post.id as postId, count(pa.id) as cnt
        from PostApplication pa
        where pa.post.id in :postIds and pa.status in :statuses
        group by pa.post.id
    """)
    List<PostCountProjection> countByPostIdsAndStatusIn(
            @Param("postIds") Collection<Long> postIds,
            @Param("statuses") Collection<ApplicationStatus> statuses
    );

    List<PostApplication> findAllByPost_IdAndStatusIn(Long postId, Collection<ApplicationStatus> statuses);

    boolean existsByPost_IdAndApplicant_IdAndStatusIn(
            Long postId,
            Long applicantId,
            List<ApplicationStatus> statuses
    );

    //마이페이지 신청됨서비스에 사용되는 것
    List<PostApplication> findAllByApplicant_IdAndStatusIn(Long userId, List<ApplicationStatus> statuses);

    //마이페이지
    List<PostApplication> findAllByApplicant_IdAndPost_Status(Long userId, core.hackathon02api.auth.entity.PostStatus status);

    // 현재 인원 계산용 (승인/참여자 수)
    long countByPost_IdAndStatusIn(Long postId, List<ApplicationStatus> statuses);

    // 신청중: 내가 APPROVED 이고, 게시글이 OPEN
    List<PostApplication> findAllByApplicant_IdAndStatusAndPost_Status(
            Long userId, ApplicationStatus status, PostStatus postStatus
    );

//    // 완료됨: 내가 APPROVED 이고, 게시글이 FULL
//    List<PostApplication> findAllByApplicant_IdAndStatusAndPost_Status(
//            Long userId, ApplicationStatus status, PostStatus postStatus
//    );



}
