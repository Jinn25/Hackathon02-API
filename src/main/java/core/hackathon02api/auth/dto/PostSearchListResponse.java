package core.hackathon02api.auth.dto;

import java.util.List;

public record PostSearchListResponse(
        List<PostSearchItemResponse> items
) {
    public static final PostSearchListResponse EMPTY = new PostSearchListResponse(List.of());
    public static PostSearchListResponse of(List<PostSearchItemResponse> items) {
        return (items == null || items.isEmpty()) ? EMPTY : new PostSearchListResponse(items);
    }
}
