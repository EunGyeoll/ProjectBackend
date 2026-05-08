package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostListDto {

    private Long postId;
    private String title;

    private String contentPreview;

    private String writerId;
    private String writerNickname;

    private LocalDateTime createdDate;

    private Long categoryId;
    private String categoryName;

    private int hitCount;
    private int likeCount;
    private int commentCount;

    private String representativeImagePath;

    public static PostListDto from(Post post) {

        String representativeImagePath =
                post.getImages().isEmpty()
                        ? null
                        : post.getImages().get(0).getImagePath();

        String contentText =
                post.getContent() == null
                        ? ""
                        : org.jsoup.Jsoup.parse(post.getContent()).text();

        String contentPreview =
                contentText.length() > 80
                        ? contentText.substring(0, 80) + "..."
                        : contentText;

        return PostListDto.builder()
                .postId(post.getPostId())
                .title(post.getTitle())

                .contentPreview(contentPreview)

                .writerId(post.getWriter().getMemberId())
                .writerNickname(post.getWriter().getNickName())

                .createdDate(post.getPostDate())

                .categoryId(post.getPostCategory().getCategoryId())
                .categoryName(post.getPostCategory().getCategoryName())

                .hitCount(post.getHitCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getComments().size())

                .representativeImagePath(representativeImagePath)

                .build();
    }
}