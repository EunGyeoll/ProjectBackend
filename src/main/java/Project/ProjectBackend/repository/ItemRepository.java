package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    // 특정 사용자로 조회
    @EntityGraph(attributePaths = "images")
    Slice<Item> findBySeller_MemberId(String memberId, Pageable pageable);

    // 검색어로 조회
    @EntityGraph(attributePaths = "images")
    Slice<Item> findByItemNameContainingIgnoreCase(String itemName, Pageable pageable);

    // 카테고리로 조회
    Slice<Item> findByCategory_CategoryName(String categoryName, Pageable pageable);


}
