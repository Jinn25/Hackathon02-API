package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
}