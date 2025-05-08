package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.ItemCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<ItemCategory, Long> {

    // 최상위 카테고리 조회
    List<ItemCategory> findByParentIsNull();

    // parentId로 자식 조회
    List<ItemCategory> findByParent_CategoryId(Long parentId);


    @EntityGraph(attributePaths = {"children"})
    List<ItemCategory> findByParentIsNullOrderByCategoryNameAsc();

    boolean existsByCategoryName(String categoryName);

}
