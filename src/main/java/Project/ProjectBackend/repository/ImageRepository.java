package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByImagePath(String imagePath);
}
