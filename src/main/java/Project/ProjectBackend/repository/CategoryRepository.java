package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentIsNullOrderByCategoryNameAsc();
    boolean existsByCategoryName(String categoryName);

}
