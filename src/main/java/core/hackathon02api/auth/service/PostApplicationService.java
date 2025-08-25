package core.hackathon02api.auth.service;

import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.repository.UserRepository;
import core.hackathon02api.auth.dto.PostApplicationResponse;
import core.hackathon02api.auth.entity.ApplicationStatus;
import core.hackathon02api.auth.entity.PostApplication;
import core.hackathon02api.auth.repository.PostApplicationRepository;
import core.hackathon02api.auth.entity.Post;
import core.hackathon02api.auth.entity.PostStatus;
import core.hackathon02api.auth.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class PostApplicationService {

    private static final List<ApplicationStatus> COUNT_STATUSES =
            List.of(ApplicationStatus.APPROVED, ApplicationStatus.JOINED); //정원에 포함되는 상태만

    private final PostApplicationRepository postApplicationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private int currentCountWithAuthor(Long postId) {
        long approved = postApplicationRepository.countByPost_IdAndStatusIn(postId, COUNT_STATUSES);
        return (int) approved + 1;
    }

    private boolean isFullAfterCount(Post post) {
        Integer desired = post.getDesiredMemberCount();
        if (desired == null) return false;
        return currentCountWithAuthor(post.getId()) >= desired;
    }

    public PostApplicationResponse apply(Long postId, Long applicantId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 기본 비즈니스 규칙
        if (post.getAuthor().getId().equals(applicantId)) {
            throw new IllegalStateException("작성자는 자신의 글에 신청할 수 없습니다.");
        }
        if (post.getStatus() != PostStatus.OPEN) {
            throw new IllegalStateException("신청이 불가능한 상태입니다.");
        }
        if (postApplicationRepository.existsByPost_IdAndApplicant_Id(postId, applicantId)) {
            throw new IllegalStateException("이미 신청한 사용자입니다.");
        }

        // 신청 직전에도 정원 확인(작성자 + 승인 인원 기준)
        if (isFullAfterCount(post)) {
            post.setStatus(PostStatus.FULL);
            throw new IllegalStateException("모집 인원이 가득 찼습니다.");
        }

        // 🔹 신청 즉시 승인으로 저장
        PostApplication saved = postApplicationRepository.save(
                PostApplication.builder()
                        .post(post)
                        .applicant(user)
                        .status(ApplicationStatus.APPROVED) // ← 즉시 승인
                        .build()
        );

        // 저장 후 정원 재평가 → 가득 차면 FULL 전환
        if (post.getStatus() == PostStatus.OPEN && isFullAfterCount(post)) {
            post.setStatus(PostStatus.FULL);
            // TODO: 여기서 알림/채팅방 자동 생성 훅 연결 가능
        }

        int currentWithAuthor = currentCountWithAuthor(postId);
        int desired = post.getDesiredMemberCount() == null ? 0 : post.getDesiredMemberCount();

        return new PostApplicationResponse(
                saved.getId(),
                post.getId(),
                user.getId(),
                saved.getStatus(),
                currentWithAuthor,   // ✅ 작성자 + 승인 인원
                desired,
                post.getStatus().name()
        );
    }

    /** 화면 등에서 "현재 인원"이 필요할 때 */
    @Transactional(readOnly = true)
    public int countCurrentMembers(Long postId) {
        return currentCountWithAuthor(postId);
    }
}

//    public PostApplicationResponse cancel(Long postId, Long applicantId) {
//        PostApplication app = postApplicationRepository.findByPost_IdAndApplicant_Id(postId, applicantId)
//                .orElseThrow(() -> new IllegalStateException("신청 내역이 없습니다."));
//
//        app.setStatus(ApplicationStatus.REMOVED);
//        postApplicationRepository.delete(app); // 물리 삭제가 깔끔. 소프트삭제 원하면 delete() 대신 상태만 저장
//
//        Post post = app.getPost();
//        long after = postApplicationRepository.countByPost_Id(postId);
//
//        // FULL 이었는데 취소로 인해 자리가 났다면 OPEN 으로 되돌림
//        if (post.getStatus() == PostStatus.FULL &&
//                post.getDesiredMemberCount() != null &&
//                after < post.getDesiredMemberCount()) {
//            post.setStatus(PostStatus.OPEN);
//        }
//
//        return new PostApplicationResponse(
//                app.getId(),
//                postId,
//                applicantId,
//                ApplicationStatus.REMOVED,
//                after,
//                post.getDesiredMemberCount() == null ? 0 : post.getDesiredMemberCount(),
//                post.getStatus().name()
//        );
//    }

