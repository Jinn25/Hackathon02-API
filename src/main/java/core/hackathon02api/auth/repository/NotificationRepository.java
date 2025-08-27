package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.Notification;
import core.hackathon02api.auth.entity.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 전체 조회(정렬 동일)
    List<Notification> findByUserIdOrderByIdDesc(Long userId);
    List<Notification> findByUserIdAndIsReadOrderByIdDesc(Long userId, boolean isRead);
    List<Notification> findByUserIdAndTypeInOrderByIdDesc(Long userId, List<NotificationType> types);
    List<Notification> findByUserIdAndIsReadAndTypeInOrderByIdDesc(Long userId, boolean isRead, List<NotificationType> types);

    // 폴링(updates)
    List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, Instant since);

    long countByUserIdAndIsReadFalse(Long userId);

    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
