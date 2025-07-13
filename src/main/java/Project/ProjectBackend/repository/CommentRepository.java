package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Comment;
import Project.ProjectBackend.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Slice<Comment> findByPostOrderByCommentDateAsc(Post post, Pageable pageable); // 특정 게시글의 댓글 조회
    void deleteAllByPost_PostId(Long postId);
}
