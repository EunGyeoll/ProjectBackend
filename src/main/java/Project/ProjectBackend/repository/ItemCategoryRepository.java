package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.ItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemCategoryRepository extends JpaRepository<ItemCategory,Long> {
    List<ItemCategory> findByParentIsNull();
    List<ItemCategory> findByParentCategoryId(Long id);

}
