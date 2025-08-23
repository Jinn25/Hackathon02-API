package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.PostApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostApplicationRepository extends JpaRepository<PostApplication, Long> {

    long countByPost_Id(Long postId);

    boolean existsByPost_IdAndApplicant_Id(Long postId, Long applicantId);

    Optional<PostApplication> findByPost_IdAndApplicant_Id(Long postId, Long applicantId);
}