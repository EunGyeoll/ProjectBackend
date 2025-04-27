package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.PostCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostCategoryRepository extends JpaRepository<PostCategory, Long> {
    Optional<PostCategory> findByCategoryName(String categoryName);
    // 카테고리 이름 존재 여부
    boolean existsByCategoryName(String categoryName);
}
