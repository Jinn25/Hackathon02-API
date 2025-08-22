package core.hackathon02api.auth.service;

import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.repository.UserRepository;
import core.hackathon02api.auth.dto.PostCreateRequest;
import core.hackathon02api.auth.dto.PostResponse;
import core.hackathon02api.auth.dto.PostUpdateRequest;
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

        return PostResponse.of(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public PostResponse get(Long id) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        return PostResponse.of(p);
    }

    @Transactional(readOnly = true)
    public Page<PostResponse> list(Pageable pageable) {
        return postRepository.findAll(pageable).map(PostResponse::of);
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

        return PostResponse.of(p);
    }

    public void softDelete(Long id, Long requesterId) {
        Post p = postRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글 없음"));
        if (!p.getAuthor().getId().equals(requesterId)) {
            throw new IllegalStateException("작성자만 삭제 가능");
        }
        p.setStatus(PostStatus.DELETED); // 소프트 삭제
    }
}