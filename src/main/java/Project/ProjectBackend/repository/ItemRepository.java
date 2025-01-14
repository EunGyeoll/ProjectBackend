package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
//    List<Item> findBySeller(Member seller); // 특정 판매자가 등록한 상품 조회
    Slice<Item> findBySeller_MemberIdOrderByItemDateDesc(String memberId, Pageable pageable);
}
