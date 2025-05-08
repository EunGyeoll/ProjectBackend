package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
    List<PostCategory> findByGroupNameOrderByCategoryNameAsc(String categoryName);
    boolean existsByCategoryName(String categoryName);
    List<PostCategory> findByGroupNameOrderBySortOrderAsc(String groupName);

}
