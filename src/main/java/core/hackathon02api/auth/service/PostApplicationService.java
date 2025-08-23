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

@Service
@RequiredArgsConstructor
@Transactional
public class PostApplicationService {

    private final PostApplicationRepository postApplicationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public PostApplicationResponse apply(Long postId, Long applicantId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다."));
        User user = userRepository.findById(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (post.getAuthor().getId().equals(applicantId)) {
            throw new IllegalStateException("작성자는 자신의 글에 신청할 수 없습니다.");
        }
        if (post.getStatus() != PostStatus.OPEN) {
            throw new IllegalStateException("신청이 불가능한 상태입니다.");
        }
        if (postApplicationRepository.existsByPost_IdAndApplicant_Id(postId, applicantId)) {
            throw new IllegalStateException("이미 신청한 사용자입니다.");
        }

        long current = postApplicationRepository.countByPost_Id(postId);
        if (post.getDesiredMemberCount() != null && current >= post.getDesiredMemberCount()) {
            // 가득 찼으면 상태를 FULL 로 바꾼다(자동)
            post.setStatus(PostStatus.FULL);
            throw new IllegalStateException("모집 인원이 가득 찼습니다.");
        }

        PostApplication saved = postApplicationRepository.save(
                PostApplication.builder()
                        .post(post)
                        .applicant(user)
                        .status(ApplicationStatus.APPLIED)
                        .build()
        );

        long after = postApplicationRepository.countByPost_Id(postId);
        // 가득 찼으면 게시글 상태 업데이트
        if (post.getDesiredMemberCount() != null &&
                after >= post.getDesiredMemberCount() &&
                post.getStatus() == PostStatus.OPEN) {
            post.setStatus(PostStatus.FULL);
            // TODO: 여기서 채팅방 자동 생성 + 알림 전송 훅 연결 가능
        }

        return new PostApplicationResponse(
                saved.getId(),
                post.getId(),
                user.getId(),
                saved.getStatus(),
                after,
                post.getDesiredMemberCount() == null ? 0 : post.getDesiredMemberCount(),
                post.getStatus().name()
        );
    }

    public PostApplicationResponse cancel(Long postId, Long applicantId) {
        PostApplication app = postApplicationRepository.findByPost_IdAndApplicant_Id(postId, applicantId)
                .orElseThrow(() -> new IllegalStateException("신청 내역이 없습니다."));

        app.setStatus(ApplicationStatus.REMOVED);
        postApplicationRepository.delete(app); // 물리 삭제가 깔끔. 소프트삭제 원하면 delete() 대신 상태만 저장

        Post post = app.getPost();
        long after = postApplicationRepository.countByPost_Id(postId);

        // FULL 이었는데 취소로 인해 자리가 났다면 OPEN 으로 되돌림
        if (post.getStatus() == PostStatus.FULL &&
                post.getDesiredMemberCount() != null &&
                after < post.getDesiredMemberCount()) {
            post.setStatus(PostStatus.OPEN);
        }

        return new PostApplicationResponse(
                app.getId(),
                postId,
                applicantId,
                ApplicationStatus.REMOVED,
                after,
                post.getDesiredMemberCount() == null ? 0 : post.getDesiredMemberCount(),
                post.getStatus().name()
        );
    }

    @Transactional(readOnly = true)
    public long count(Long postId) {
        return postApplicationRepository.countByPost_Id(postId);
    }
}