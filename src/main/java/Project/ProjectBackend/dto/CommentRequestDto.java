package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Comment;
import Project.ProjectBackend.entity.Post;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@RequiredArgsConstructor
public class CommentRequestDto {

    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    private String content;
    private Long parentCommentId;
    private String imageUrl;
}
