package core.hackathon02api.auth.repository;

import core.hackathon02api.auth.entity.Notification;
import core.hackathon02api.auth.entity.NotificationType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 최신순 첫 로드 (id DESC) - Pageable로 limit 전달
    List<Notification> findByUserIdOrderByIdDesc(Long userId, Pageable pageable);

    // 다음 로드: lastId보다 작은 항목만 (커서)
    List<Notification> findByUserIdAndIdLessThanOrderByIdDesc(Long userId, Long lastId, Pageable pageable);

    // (옵션) 필터 조합이 필요할 때 사용 — 서비스 코드에 이미 분기 있음
    List<Notification> findByUserIdAndIsReadOrderByIdDesc(Long userId, boolean isRead, Pageable pageable);
    List<Notification> findByUserIdAndIsReadAndIdLessThanOrderByIdDesc(Long userId, boolean isRead, Long lastId, Pageable pageable);

    List<Notification> findByUserIdAndTypeInOrderByIdDesc(Long userId, List<NotificationType> types, Pageable pageable);
    List<Notification> findByUserIdAndTypeInAndIdLessThanOrderByIdDesc(Long userId, List<NotificationType> types, Long lastId, Pageable pageable);

    List<Notification> findByUserIdAndIsReadAndTypeInOrderByIdDesc(Long userId, boolean isRead, List<NotificationType> types, Pageable pageable);
    List<Notification> findByUserIdAndIsReadAndTypeInAndIdLessThanOrderByIdDesc(Long userId, boolean isRead, List<NotificationType> types, Long lastId, Pageable pageable);

    // 폴링(updates)용 (createdAt 기준) — 기존 유지
    List<Notification> findByUserIdAndCreatedAtAfterOrderByCreatedAtDesc(Long userId, Instant since);

    long countByUserIdAndIsReadFalse(Long userId);
    Optional<Notification> findByIdAndUserId(Long id, Long userId);
}
