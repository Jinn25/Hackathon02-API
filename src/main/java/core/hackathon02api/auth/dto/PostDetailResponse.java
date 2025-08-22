package core.hackathon02api.auth.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
public class PostDetailResponse {
    private Long id;
    private AuthorResponse author;  // 작성자 정보
    private String title;
    private String category;
    private String productName;
    private String productUrl;
    private String productDesc;
    private Integer desiredMemberCount;
    private String content;
    private String mainImageUrl;
    private String status;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDateTime createdAt;

}
