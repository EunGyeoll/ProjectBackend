package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.ItemCategory;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<ItemCategory, Long> {

    @EntityGraph(attributePaths = {"children"})
    List<ItemCategory> findByParentIsNullOrderByCategoryNameAsc();

    boolean existsByCategoryName(String categoryName);

}
