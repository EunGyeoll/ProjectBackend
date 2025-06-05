package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // 검색
    @Query("SELECT i FROM Item i WHERE LOWER(i.itemName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Slice<Item> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);


    // 키워드+카테고리로 검색
    @Query("SELECT i FROM Item i WHERE " +
            "(LOWER(i.itemName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(i.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND i.category.categoryName = :category")
    Slice<Item> searchByKeywordAndCategory(@Param("keyword") String keyword,
                                           @Param("category") String category,
                                           Pageable pageable);


}
