package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class PostListDto {
    private Long postNo;
    private String title;
    private String writerId;
    private LocalDateTime createdDate;
    private Long categoryId;
    private String categoryName;
    private int likeCount;

    public static PostListDto from(Post post) {
        return PostListDto.builder()
                .postNo(post.getPostNo())
                .title(post.getTitle())
                .writerId(post.getWriter().getMemberId())
                .createdDate(post.getPostDate())
                .categoryId(post.getPostCategory().getCategoryId())
                .categoryName(post.getPostCategory().getCategoryName())
                .likeCount(post.getLikeCount())
                .build();
    }
}

