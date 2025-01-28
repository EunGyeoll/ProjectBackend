package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.LikedPost;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class LikedPostDto {
    private Long likedId; // 좋아요 ID
    private String memberId; // 좋아요 누른 회원의 ID
    private Long postId; // 좋아요가 눌린 게시글의 ID
    private String representativeImagePath; // 게시글의 대표 이미지 경로
    private LocalDateTime createdAt; // 좋아요 누른 시간

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static LikedPostDto from(LikedPost likedPost) {
        return LikedPostDto.builder()
                .likedId(likedPost.getLikedId())
                .memberId(likedPost.getMember().getMemberId()) // 좋아요 누른 회원 ID
                .postId(likedPost.getPost().getPostNo()) // 좋아요 눌린 게시글 ID
                .representativeImagePath(likedPost.getPost().getRepresentativeImagePath()) // 게시글의 대표 이미지 경로
                .createdAt(likedPost.getCreatedAt()) // 좋아요 누른 시간
                .build();
    }
}
