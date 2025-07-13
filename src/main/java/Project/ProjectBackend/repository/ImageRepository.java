package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findByImagePath(String imagePath);

    // 게시글에 속한 모든 이미지 삭제
    void deleteAllByPost_PostId(Long postId);

    // S3 삭제를 위해 먼저 이미지 경로 리스트 조회
    List<Image> findAllByPost_PostId(Long postId);
}
