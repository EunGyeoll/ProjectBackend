package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Comment;
import Project.ProjectBackend.entity.Post;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor

public class PostResponseDto {
    private Long postNo;
    private String writerName; // 작성자의 이름
    private String title;
    private String content;
    private LocalDateTime postDate;
    private List<CommentResponseDto> comments;
    private int hitCount=0;

    public PostResponseDto(Post post) {
        this.postNo = post.getPostNo();
        this.writerName = post.getWriter().getName(); // Member 엔티티의 name 필드 사용
        this.title = post.getTitle();
        this.content = post.getContent();
        this.postDate = post.getPostDate();
        this.comments=post.getComments().stream()
                .map(CommentResponseDto::new) // Comment -> CommentResponseDto 변환
                .collect(Collectors.toList());
        this.hitCount = post.getHitCount();
    }

    public static PostResponseDto from(Post post) {
        PostResponseDto postResponseDto = new PostResponseDto();
        postResponseDto.postNo = post.getPostNo();
        postResponseDto.writerName = post.getWriter().getName();
        postResponseDto.title = post.getTitle();
        postResponseDto.content = post.getContent();
        postResponseDto.postDate = post.getPostDate();
        postResponseDto.hitCount = post.getHitCount();
        return postResponseDto;
    }
}
