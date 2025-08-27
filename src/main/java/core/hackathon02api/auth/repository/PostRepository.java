package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.Post;
import core.hackathon02api.auth.entity.PostApplication;
import core.hackathon02api.auth.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, PostSearchRepository {

    // 내가 쓴 글
    Page<Post> findByAuthor_IdOrderByCreatedAtDesc(Long authorId, Pageable pageable);

    // 신청중: OPEN, FULL
    @Query("""
        select pa from PostApplication pa
        join fetch pa.post p
        where pa.applicant.id = :userId
          and p.status in :postStatuses
        order by pa.createdAt desc
        """)
    Page<PostApplication> findApplicationsByApplicantAndPostStatuses(
            @Param("userId") Long userId,
            @Param("postStatuses") Collection<PostStatus> postStatuses,
            Pageable pageable
    );

    // 완료됨: COMPLETED
    @Query("""
        select pa from PostApplication pa
        join fetch pa.post p
        where pa.applicant.id = :userId
          and p.status = core.hackathon02api.auth.entity.PostStatus.COMPLETED
        order by pa.createdAt desc
        """)
    Page<PostApplication> findCompletedApplicationsByApplicant(
            @Param("userId") Long userId,
            Pageable pageable
    );

    List<Post> findAllByAuthor_IdOrderByCreatedAtDesc(Long authorId);

}