package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Comment;
import Project.ProjectBackend.entity.Post;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
public class CommentRequestDto {
    private Post post;
    private String writerId; // 댓글 작성자
    private String content;
    private LocalDateTime commentDate;
    private Comment parentComment;
    private List<Comment> childComments;

}
