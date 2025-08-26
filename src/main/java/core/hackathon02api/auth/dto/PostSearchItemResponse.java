package core.hackathon02api.auth.dto;

public record PostSearchItemResponse(
        Long id,
        String title,
        String category,
        String productName,
        Long price,          // Post.price 없으면 null
        String imageUrl,     // mainImageUrl -> imageUrls[0] -> null
        Integer currentMemberCount, // 작성자 1 + 승인 수
        Integer desiredMemberCount,
        String status,       // OPEN/FULL/...
        String createdAt
) {}
