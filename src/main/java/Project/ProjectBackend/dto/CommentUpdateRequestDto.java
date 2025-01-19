package Project.ProjectBackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentUpdateRequestDto {

    @NotBlank(message = "댓글 내용은 비워둘 수 없습니다.")
    private String content;

}
