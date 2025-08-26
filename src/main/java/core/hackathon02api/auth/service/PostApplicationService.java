package core.hackathon02api.auth.service;

import core.hackathon02api.auth.entity.*;
import core.hackathon02api.auth.repository.UserRepository;
import core.hackathon02api.auth.dto.PostApplicationResponse;
import core.hackathon02api.auth.repository.PostApplicationRepository;
import core.hackathon02api.auth.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class PostApplicationService {

    private static final List<ApplicationStatus> COUNT_STATUSES =
            List.of(ApplicationStatus.APPROVED, ApplicationStatus.JOINED);

    private final PostApplicationRepository postApplicationRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    // 알림 서비스 주입
    private final NotificationService notificationService;

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

        if (post.getAuthor().getId().equals(applicantId)) {
            throw new IllegalStateException("작성자는 자신의 글에 신청할 수 없습니다.");
        }
        if (post.getStatus() != PostStatus.OPEN) {
            throw new IllegalStateException("신청이 불가능한 상태입니다.");
        }
        if (postApplicationRepository.existsByPost_IdAndApplicant_Id(postId, applicantId)) {
            throw new IllegalStateException("이미 신청한 사용자입니다.");
        }

        if (isFullAfterCount(post)) {
            post.setStatus(PostStatus.FULL);
            throw new IllegalStateException("모집 인원이 가득 찼습니다.");
        }

        // 신청 즉시 승인 저장
        PostApplication saved = postApplicationRepository.save(
                PostApplication.builder()
                        .post(post)
                        .applicant(user)
                        .status(ApplicationStatus.APPROVED)
                        .build()
        );

        // 🔔 호스트에게 "신청(자동 승인)" 알림
        int currentWithAuthor = currentCountWithAuthor(postId);
        int desired = Optional.ofNullable(post.getDesiredMemberCount()).orElse(0);
        notificationService.notify(
                post.getAuthor().getId(),
                NotificationType.POST_APPLIED,
                "새로운 신청이 도착했어요",
                user.getNickname() + "님이 신청했고 자동 승인되었습니다. (" + currentWithAuthor + "/" + desired + ")",
                post.getId()
        );

        // 정원 재평가 → 가득 차면 FULL 전환 + 브로드캐스트 알림
        if (post.getStatus() == PostStatus.OPEN && isFullAfterCount(post)) {
            post.setStatus(PostStatus.FULL);

            // 호스트에게
            notificationService.notify(
                    post.getAuthor().getId(),
                    NotificationType.POST_FULL,
                    "모집이 완료되었습니다!",
                    "현재 인원 " + currentWithAuthor + "/" + desired + "로 마감되었습니다.",
                    post.getId()
            );

            // 승인된 참여자 전원에게
            var members = postApplicationRepository.findAllByPost_IdAndStatusIn(post.getId(), COUNT_STATUSES);
            for (PostApplication pa : members) {
                notificationService.notify(
                        pa.getApplicant().getId(),
                        NotificationType.POST_FULL,
                        "모집이 완료되었습니다!",
                        "참여 중인 \"" + post.getTitle() + "\"이(가) 마감되었습니다. (" + currentWithAuthor + "/" + desired + ")",
                        post.getId()
                );
            }
        }

        return new PostApplicationResponse(
                saved.getId(),
                post.getId(),
                user.getId(),
                saved.getStatus(),
                currentWithAuthor,
                desired,
                post.getStatus().name()
        );
    }

    @Transactional(readOnly = true)
    public int countCurrentMembers(Long postId) {
        return currentCountWithAuthor(postId);
    }
}

