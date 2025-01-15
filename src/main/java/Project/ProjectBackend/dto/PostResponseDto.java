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

    // 게시글 단일 조회를 위한 Dto 변환 메서드
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


    // 게시글 목록 조회를 위한 Dto 변환 메서드
    public static PostResponseDto fromForList(Post post) {
        Member writer = post.getWriter();

        // 대표 이미지 경로
        String representativeImagePath = post.getImages().isEmpty()
                ? null
                : post.getImages().get(0).getImagePath();

        return new PostResponseDto(
                post.getPostNo(),
                post.getTitle(),
                null, // 목록 조회에서는 내용을 포함하지 않음
                writer != null ? writer.getName() : null,
                writer != null ? writer.getEmail() : null,
                post.getPostDate(),
                post.getHitCount(),
                post.getLikeCount(),
                representativeImagePath,
                null, // 목록 조회에서는 이미지 경로 리스트를 포함하지 않음
                0 // 목록 조회에서는 댓글 수를 포함하지 않음
        );
    }
}
