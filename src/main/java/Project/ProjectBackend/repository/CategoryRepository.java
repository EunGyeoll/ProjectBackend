package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    @EntityGraph(attributePaths = {"children"})
    List<Category> findByParentIsNullOrderByCategoryNameAsc();

    boolean existsByCategoryName(String categoryName);

}
