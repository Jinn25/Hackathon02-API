package core.hackathon02api.auth.service;

import core.hackathon02api.auth.dto.*;
import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.repository.PostApplicationRepository;
import core.hackathon02api.auth.repository.UserRepository;
import core.hackathon02api.auth.entity.Post;
import core.hackathon02api.auth.entity.PostStatus;
import core.hackathon02api.auth.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostApplicationRepository postApplicationRepository;

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

        return PostResponse.of(postRepository.save(post),0);
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        int current = (int) postApplicationRepository.countByPost_Id(id); // ✅
        return PostResponse.of(p,current);
    }

//    @Transactional(readOnly = true)
//    public Page<PostResponse> list(Pageable pageable) {
//        return postRepository.findAll(pageable).map(PostResponse::of);
//    }
    @Transactional(readOnly = true)
    public Page<PostResponse> list(Pageable pageable) {
        return postRepository.findAll(pageable)
                .map(p -> PostResponse.of(
                        p,
                        (int) postApplicationRepository.countByPost_Id(p.getId()) // ✅ 간단 버전
                ));
        // ※ 성능 최적화 필요하면 추후 postIds로 group by count 쿼리 한 번에 가져오도록 개선
    }

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
        int current = (int) postApplicationRepository.countByPost_Id(p.getId()); // ✅

        return PostResponse.of(p,current);
    }

    public void softDelete(Long id, Long requesterId) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        if (!p.getAuthor().getId().equals(requesterId)) {
            throw new IllegalStateException("작성자만 삭제 가능");
        }
        p.setStatus(PostStatus.DELETED); // 소프트 삭제
    }

    public PostDetailResponse getPostDetail(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

        long currentCount = postApplicationRepository.countByPost_Id(postId);  // ✅ 신청 인원 조회

        PostDetailResponse response = new PostDetailResponse();
        response.setId(post.getId());

        // 작성자 매핑
        AuthorResponse authorDto = new AuthorResponse();
        authorDto.setId(post.getAuthor().getId());
        authorDto.setNickname(post.getAuthor().getNickname());
        authorDto.setRoadAddress(post.getAuthor().getRoadAddress()); // 추가!
        response.setAuthor(authorDto);

        response.setTitle(post.getTitle());
        response.setCategory(post.getCategory());
        response.setProductName(post.getProductName());
        response.setProductUrl(post.getProductUrl());
        response.setProductDesc(post.getProductDesc());
        response.setDesiredMemberCount(post.getDesiredMemberCount());
        response.setCurrentMemberCount((int)currentCount);   // ✅ 여기 추가
        response.setContent(post.getContent());
        response.setMainImageUrl(post.getMainImageUrl());
        response.setStatus(String.valueOf(post.getStatus()));
        response.setCreatedAt(post.getCreatedAt());

        return response;
    }
}