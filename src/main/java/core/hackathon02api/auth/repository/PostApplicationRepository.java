package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.PostApplication;
import core.hackathon02api.auth.entity.ApplicationStatus;
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

    // 특정 게시글의 승인 인원 수
    long countByPost_IdAndStatusIn(Long postId, Collection<ApplicationStatus> statuses);

    // 여러 게시글의 승인 인원 수를 group by로
    interface PostCountProjection {
        Long getPostId();
        long getCnt();
    }

    @Query("""
        select pa.post.id as postId, count(pa.id) as cnt
        from PostApplication pa
        where pa.post.id in :postIds and pa.status in :statuses
        group by pa.post.id
    """)
    List<PostCountProjection> countByPostIdsAndStatusIn(@Param("postIds") Collection<Long> postIds,
                                                        @Param("statuses") Collection<ApplicationStatus> statuses);

    // 추가 메서드들
    List<PostApplication> findAllByPost_IdAndStatusIn(Long postId, Collection<ApplicationStatus> statuses);

    boolean existsByPost_IdAndApplicant_IdAndStatusIn(Long postId,
                                                      Long applicantId,
                                                      List<ApplicationStatus> statuses);
           
    List<PostCountProjection> countApprovedByPostIds(Collection<Long> postIds, Collection<ApplicationStatus> statuses);
    List<PostApplication> findAllByPost_IdAndStatusIn(Long postId, Collection<ApplicationStatus> statuses);
}
