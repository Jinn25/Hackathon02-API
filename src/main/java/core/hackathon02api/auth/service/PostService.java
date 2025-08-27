package core.hackathon02api.auth.service;

import core.hackathon02api.auth.dto.*;
import core.hackathon02api.auth.entity.Post;
import core.hackathon02api.auth.entity.PostStatus;
import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.repository.PostApplicationRepository;
import core.hackathon02api.auth.repository.PostRepository;
import core.hackathon02api.auth.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostApplicationRepository postApplicationRepository;

    // ===== 생성 =====
    public PostResponse create(Long authorId, PostCreateRequest req) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new IllegalArgumentException("작성자 없음"));

        Post post = Post.builder()
                .author(author)
                .title(req.getTitle())
                .category(req.getCategory())
                .productName(req.getProductName())
                .productUrl(req.getProductUrl())
                .productDesc(req.getProductDesc())
                .desiredMemberCount(req.getDesiredMemberCount())
                .content(req.getContent())
                .mainImageUrl(req.getMainImageUrl())
                .imageUrls(req.getImageUrls())
                .build();

        Post saved = postRepository.save(post);
        return PostResponse.of(saved, 0);
    }

    // ===== 단건 조회 =====
    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        int current = (int) postApplicationRepository.countByPost_Id(p.getId());
        return PostResponse.of(p, current);
    }

    // ===== 전체 목록 (Full-Fetch) — 상태 필터 선택 =====
    @Transactional(readOnly = true)
    public List<PostResponse> listAll(List<PostStatus> statuses) {
        final List<Post> posts = (statuses == null || statuses.isEmpty())
                ? postRepository.findAllByOrderByCreatedAtDesc()
                : postRepository.findAllByStatusInOrderByCreatedAtDesc(statuses);

        if (posts.isEmpty()) return List.of();

        // 신청 인원 수 집계 (group-by, 한 방 쿼리)
        List<Long> postIds = posts.stream().map(Post::getId).toList();
        Map<Long, Integer> currentByPost = postApplicationRepository.countByPostIdsGroup(postIds).stream()
                .collect(Collectors.toMap(
                        PostApplicationRepository.PostCountProjection::getPostId,
                        p -> (int) p.getCnt()
                ));

        return posts.stream()
                .map(p -> PostResponse.of(p, currentByPost.getOrDefault(p.getId(), 0)))
                .toList();
    }

    // ===== 부분 수정 =====
    public PostResponse update(Long id, Long requesterId, PostUpdateRequest req) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));

        if (!p.getAuthor().getId().equals(requesterId)) {
            throw new IllegalStateException("작성자만 수정 가능");
        }

        if (req.getTitle() != null) p.setTitle(req.getTitle());
        if (req.getCategory() != null) p.setCategory(req.getCategory());
        if (req.getProductName() != null) p.setProductName(req.getProductName());
        if (req.getProductUrl() != null) p.setProductUrl(req.getProductUrl());
        if (req.getProductDesc() != null) p.setProductDesc(req.getProductDesc());
        if (req.getDesiredMemberCount() != null) p.setDesiredMemberCount(req.getDesiredMemberCount());
        if (req.getContent() != null) p.setContent(req.getContent());
        if (req.getMainImageUrl() != null) p.setMainImageUrl(req.getMainImageUrl());
        if (req.getImageUrls() != null) p.setImageUrls(req.getImageUrls());
        if (req.getStatus() != null) p.setStatus(PostStatus.valueOf(req.getStatus()));

        int current = (int) postApplicationRepository.countByPost_Id(p.getId());
        return PostResponse.of(p, current);
    }

    // ===== 소프트 삭제 =====
    public void softDelete(Long id, Long requesterId) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        if (!p.getAuthor().getId().equals(requesterId)) {
            throw new IllegalStateException("작성자만 삭제 가능");
        }
        p.setStatus(PostStatus.DELETED);
    }

    // ===== 상세 조회 (확장 DTO) =====
    @Transactional(readOnly = true)
    public PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        long currentCount = postApplicationRepository.countByPost_Id(postId);

        PostDetailResponse response = new PostDetailResponse();
        response.setId(post.getId());

        // 작성자
        AuthorResponse authorDto = new AuthorResponse();
        authorDto.setId(post.getAuthor().getId());
        authorDto.setNickname(post.getAuthor().getNickname());
        authorDto.setRoadAddress(post.getAuthor().getRoadAddress());
        response.setAuthor(authorDto);

        response.setTitle(post.getTitle());
        response.setCategory(post.getCategory());
        response.setProductName(post.getProductName());
        response.setProductUrl(post.getProductUrl());
        response.setProductDesc(post.getProductDesc());
        response.setDesiredMemberCount(post.getDesiredMemberCount());
        response.setCurrentMemberCount((int) currentCount);
        response.setContent(post.getContent());
        response.setMainImageUrl(post.getMainImageUrl());
        response.setStatus(String.valueOf(post.getStatus()));
        response.setCreatedAt(post.getCreatedAt());

        return response;
    }

    public void delete(Long postId, Long requesterId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("게시글을 찾을 수 없습니다."));

//        if (!post.isOwner(requesterId)) {
//            throw new AccessDeniedException("삭제 권한이 없습니다.");
//        }

        postRepository.delete(post); // 완전 삭제
    }
}
