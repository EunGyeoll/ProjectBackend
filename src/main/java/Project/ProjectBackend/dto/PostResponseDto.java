package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class PostResponseDto {

    private Long postNo; // 게시글 번호
    private String title;
    private String content;
    private String writerName;
    private String writerEmail;
    private LocalDateTime postDate;
    private int hitCount;
    private int likeCount; // 좋아요 수
    private String representativeImagePath; // 대표 이미지 경로
    private List<String> imagePaths; // 이미지 경로 리스트
    private int commentCount; // 댓글 수

    // Post 엔티티를 기반으로 Dto 객체를 생성하는 메서드
    public static PostResponseDto from(Post post) {
        Member writer = post.getWriter();

        // 이미지 경로 리스트 생성
        List<String> imagePaths = post.getImages().stream()
                .map(Image::getImagePath)
                .collect(Collectors.toList());

        // 대표 이미지 경로
        String representativeImagePath = imagePaths.isEmpty() ? null : imagePaths.get(0);

        // 댓글 수 계산
        int commentCount = post.getComments().size();

        return new PostResponseDto(
                post.getPostNo(),
                post.getTitle(),
                post.getContent(),
                writer != null ? writer.getName() : null, // 작성자 이름
                writer != null ? writer.getEmail() : null, // 작성자 이메일
                post.getPostDate(),
                post.getHitCount(),
                post.getLikeCount(),
                representativeImagePath,
                imagePaths,
                commentCount
        );
    }
}
