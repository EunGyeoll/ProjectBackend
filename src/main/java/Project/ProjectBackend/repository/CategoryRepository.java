package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
