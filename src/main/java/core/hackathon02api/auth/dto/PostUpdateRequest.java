package core.hackathon02api.auth.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter
public class PostUpdateRequest {
    private String title;
    private String category;
    private String productName;
    private String productUrl;
    private String productDesc;
    private Integer desiredMemberCount;
    private String content;
    private String mainImageUrl;
    private List<String> imageUrls;
    private String status; // "OPEN|FULL|COMPLETED|DELETED"
}