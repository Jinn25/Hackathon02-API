package core.hackathon02api.auth.service;

import core.hackathon02api.auth.dto.MyPagePostCard;
import core.hackathon02api.auth.dto.PostResponse;
import core.hackathon02api.auth.entity.ApplicationStatus;
import core.hackathon02api.auth.entity.PostApplication;
import core.hackathon02api.auth.entity.PostStatus;
import core.hackathon02api.auth.repository.PostApplicationRepository;
import core.hackathon02api.auth.repository.PostRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MyPageService {

    private final PostApplicationRepository postApplicationRepository;
    private final PostRepository postRepository;
    // 현재 인원: (APPROVED or JOINED) + 작성자 1
    private int currentCountWithAuthor(Long postId) {
        long approvedOrJoined = postApplicationRepository.countByPost_IdAndStatusIn(
                postId, List.of(ApplicationStatus.APPROVED, ApplicationStatus.JOINED)
        );
        return (int) approvedOrJoined + 1;
    }

    /** 신청중: 내가 APPROVED 이고, 게시글이 OPEN */
    public List<PostResponse> getAppliedOngoing(Long userId) {
        var apps = postApplicationRepository.findAllByApplicant_IdAndStatusAndPost_Status(
                userId, ApplicationStatus.APPROVED, PostStatus.OPEN
        );
        return apps.stream()
                .map(PostApplication::getPost)
                .distinct()
                .map(p -> PostResponse.of(p, currentCountWithAuthor(p.getId())))
                .toList();
    }

    /** 완료됨: 내가 APPROVED 이고, 게시글이 FULL */
    public List<PostResponse> getAppliedCompleted(Long userId) {
        var apps = postApplicationRepository.findAllByApplicant_IdAndStatusAndPost_Status(
                userId, ApplicationStatus.APPROVED, PostStatus.FULL
        );
        return apps.stream()
                .map(PostApplication::getPost)
                .distinct()
                .map(p -> PostResponse.of(p, currentCountWithAuthor(p.getId())))
                .toList();
    }

    public List<PostResponse> getMyPosts(Long authorId) {
        var posts = postRepository.findAllByAuthor_IdOrderByCreatedAtDesc(authorId);
        return posts.stream()
                .map(p -> PostResponse.of(p, currentCountWithAuthor(p.getId())))
                .toList();
    }
}
