package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.LikedPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class LikedPostListDto {
    private Long likedId; // 좋아요 ID
    private String memberId; // 좋아요 누른 회원의 ID
    private Long postNo; // 좋아요가 눌린 게시글의 ID
    private String title;
    private LocalDateTime postDate;
    private String representativeImagePath; // 게시글의 대표 이미지 경로
    private LocalDateTime createdAt; // 좋아요 누른 시간

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static LikedPostListDto from(LikedPost likedPost) {
        return LikedPostListDto.builder()
                .likedId(likedPost.getLikedId())
                .memberId(likedPost.getMember().getMemberId()) // 좋아요 누른 회원 ID
                .postNo(likedPost.getPost().getPostId()) // 좋아요 눌린 게시글 ID
                .title(likedPost.getPost().getTitle())
                .postDate(likedPost.getPost().getPostDate())
                .representativeImagePath(likedPost.getPost().getRepresentativeImagePath()) // 게시글의 대표 이미지 경로
                .createdAt(likedPost.getCreatedAt()) // 좋아요 누른 시간
                .build();
    }
}
