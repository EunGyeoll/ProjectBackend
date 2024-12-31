package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Comment;
import Project.ProjectBackend.entity.Post;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PostResponseDto {
    private Long boardNo;
    private String writerName; // 작성자의 이름
    private String title;
    private String content;
    private LocalDateTime postDate;
    private List<Comment> comments;

    public PostResponseDto(Post post) {
        this.boardNo = post.getPostNo();
        this.writerName = post.getWriter().getName(); // Member 엔티티의 name 필드 사용
        this.title = post.getTitle();
        this.content = post.getContent();
        this.postDate = post.getPostDate();
        this.comments=post.getComments();
    }
}
