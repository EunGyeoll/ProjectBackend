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
    private Long parentCommentId;
    private String imageUrl;
    private boolean isDeleted;


    public CommentResponseDto(Comment comment) {
        this.commentId = comment.getCommentId();                // 댓글 ID
        this.writerId = comment.getWriter().getMemberId();      // 작성자의 ID
        this.writerName = comment.getWriter().getMemberName();        // 작성자의 이름
        this.content = comment.getContent();                    // 댓글 내용
        this.commentDate = comment.getCommentDate();
        this.parentCommentId = comment.getParentComment() != null ? comment.getParentComment().getCommentId() : null;
        this.imageUrl = comment.getImageUrl();
        this.isDeleted = comment.isDeleted();
    }
}
