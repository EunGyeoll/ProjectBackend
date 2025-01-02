package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Comment;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CommentResponseDto {
    private Long commentId;      // 댓글 ID
    private String writerId;     // 작성자의 ID (Member 엔티티의 ID)
    private String writerName;   // 작성자의 이름
    private String content;      // 댓글 내용
    private LocalDateTime commentDate; // 댓글 작성일

    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getCommentId();                // 댓글 ID
        this.writerId = comment.getWriter().getMemberId();      // 작성자의 ID
        this.writerName = comment.getWriter().getName();        // 작성자의 이름
        this.content = comment.getContent();                    // 댓글 내용
        this.commentDate = comment.getCommentDate();            // 댓글 작성일
    }
}
