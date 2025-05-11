package Project.ProjectBackend.dto;

import Project.ProjectBackend.controller.AdminController;
import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Post;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@Builder
public class PostResponseDto {

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    private Long postNo; // 게시글 번호
    private String title;
    private String content;
    private String writerId; // writerName 은 memberId로
    private String writerNickname;
    private String profileImageUrl;
    private Long categoryId;
    private String categoryName;
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
        logger.info("이미지 경로 목록: {}", imagePaths);

        // 댓글 수 계산
        int commentCount = post.getComments().size();

        return PostResponseDto.builder()
                .postNo(post.getPostNo())
                .title(post.getTitle())
                .content(post.getContent())
                .writerId(writer != null ? writer.getMemberId() : null)
                .writerNickname(writer != null ? writer.getNickName() : null)
                .profileImageUrl(writer != null ? writer.getProfileImageUrl() : null)
                .categoryId(post.getPostCategory().getCategoryId())
                .categoryName(post.getPostCategory().getCategoryName())
                .postDate(post.getPostDate())
                .hitCount(post.getHitCount())
                .likeCount(post.getLikeCount())
                .representativeImagePath(representativeImagePath)
                .imagePaths(imagePaths)
                .commentCount(post.getComments().size())
                .build();
    }


    // 게시글 목록 조회를 위한 Dto 변환 메서드
    public static PostResponseDto fromForList(Post post) {
        Member writer = post.getWriter();

        // 대표 이미지 경로
        String representativeImagePath = post.getImages().isEmpty()
                ? null
                : post.getImages().get(0).getImagePath();

        return PostResponseDto.builder()
                .postNo(post.getPostNo())
                .title(post.getTitle())
                .content(null) // 목록에서는 내용 제외
                .writerId(writer != null ? writer.getMemberId() : null)
                .writerNickname(writer != null ? writer.getNickName() : null)
                .profileImageUrl(writer != null ? writer.getProfileImageUrl() : null)
                .categoryName(post.getPostCategory().getCategoryName()) // ✅ 카테고리 이름 추가
                .postDate(post.getPostDate())
                .hitCount(post.getHitCount())
                .likeCount(post.getLikeCount())
                .representativeImagePath(representativeImagePath)
                .imagePaths(null) // 목록에서는 전체 이미지 리스트 제외
                .commentCount(post.getComments().size()) //  댓글 수 계산
                .build();

    }
}
