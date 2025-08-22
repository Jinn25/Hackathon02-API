package core.hackathon02api.auth.dto;

import core.hackathon02api.auth.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
public class PostResponse {
    private Long id;
    private Long authorId;
    private String authorNickname;
    private String title;
    private String category;
    private String productName;
    private String productUrl;
    private String productDesc;
    private Integer desiredMemberCount;
    private String content;
    private String mainImageUrl;
    private List<String> imageUrls;
    private String status;
    private LocalDateTime createdAt;

    public static PostResponse of(Post p) {
        return new PostResponse(
                p.getId(),
                p.getAuthor().getId(),
                p.getAuthor().getNickname(),
                p.getTitle(),
                p.getCategory(),
                p.getProductName(),
                p.getProductUrl(),
                p.getProductDesc(),
                p.getDesiredMemberCount(),
                p.getContent(),
                p.getMainImageUrl(),
                p.getImageUrls(),
                p.getStatus().name(),
                p.getCreatedAt()
        );
    }
}