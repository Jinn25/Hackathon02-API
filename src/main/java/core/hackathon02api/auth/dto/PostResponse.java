package core.hackathon02api.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
/**
 * 실제 post get 요청 날렸을 때 사용하는 클래스
 * postdetail은 어디서 쓰는지 모르겠다...
 * */
@Getter @Setter
public class PostResponse {
    private Long id;
    private AuthorResponse author;   // ✅ 중첩 객체
    private String title;
    private String category;
    private String productName;
    private String productUrl;
    private String productDesc;
    private Integer desiredMemberCount;
    private Integer currentMemberCount;
    private String content;
    private String mainImageUrl;
    private String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

    public static PostResponse of(core.hackathon02api.auth.entity.Post p,int currentCount) {
        PostResponse res = new PostResponse();
        res.setId(p.getId());

        AuthorResponse author = new AuthorResponse();
        author.setId(p.getAuthor().getId());
        author.setNickname(p.getAuthor().getNickname());
        author.setRoadAddress(p.getAuthor().getRoadAddress());
        res.setAuthor(author); // ✅ 중첩으로 세팅

        res.setTitle(p.getTitle());
        res.setCategory(p.getCategory());
        res.setProductName(p.getProductName());
        res.setProductUrl(p.getProductUrl());
        res.setProductDesc(p.getProductDesc());
        res.setDesiredMemberCount(p.getDesiredMemberCount());
        res.setCurrentMemberCount(currentCount);
        res.setContent(p.getContent());
        res.setMainImageUrl(p.getMainImageUrl());
        res.setStatus(String.valueOf(p.getStatus()));
        res.setCreatedAt(p.getCreatedAt());
        return res;
    }
}
