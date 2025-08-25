package core.hackathon02api.auth.entity;

import java.util.List;

public enum PostStatus {
    OPEN, FULL, COMPLETED, DELETED, APPROVED, JOINED;
    public static List<PostStatus> approvedLike() {
        return List.of(PostStatus.APPROVED, PostStatus.JOINED);
    }
}