package core.hackathon02api.auth.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PostCreateRequest {
    @NotBlank private String title;
    private String category;
    @NotBlank private String productName;
    private String productUrl;
    private String productDesc;
    @Min(1) private Integer desiredMemberCount;
    @NotBlank private String content;
    private String mainImageUrl;
    private List<String> imageUrls;   // 서버에서 JSON으로 저장됨
}