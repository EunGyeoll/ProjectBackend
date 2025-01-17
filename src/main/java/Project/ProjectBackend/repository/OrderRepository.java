package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Order;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Slice<Order> findByMember(Member member, Pageable pageable);

}
