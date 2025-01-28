package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Review;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // 상점 소유자가 받은 리뷰 조회 (페이징)
    Slice<Review> findByStoreOwner_MemberIdOrderByCreatedAtDesc(String storeOwnerId, Pageable pageable);
}
