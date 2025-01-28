package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Review;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReviewResponseDto {
    private Long reviewId;
    private String writerId;
    private String content;
    private int rating;
    private LocalDateTime createdAt;
    private String representativeImagePath;
    private List<String> imagePaths;

    // 게시글 단일 조회를 위한 Dto 변환 메서드
    public static ReviewResponseDto from(Review review) {
        // 이미지 경로 리스트 생성
        List<String> imagePaths = review.getImages().stream()
                .map(Image::getImagePath)
                .collect(Collectors.toList());

        // 대표 이미지 경로 설정
        String representativeImagePath = imagePaths.isEmpty() ? null : imagePaths.get(0);

        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .writerId(review.getWriter().getMemberId())
                .content(review.getContent())
                .rating(review.getRating())
                .representativeImagePath(representativeImagePath)
                .imagePaths(imagePaths) // 이미지 경로 포함
                .createdAt(review.getCreatedAt())
                .build();
    }

    // 리뷰 목록 조회를 위한 Dto 변환 메서드
    public static ReviewResponseDto fromForList(Review review) {
        return ReviewResponseDto.builder()
                .reviewId(review.getReviewId())
                .writerId(review.getWriter().getMemberId())
                .rating(review.getRating())
                .representativeImagePath(
                        review.getImages().isEmpty() ? null : review.getImages().get(0).getImagePath()
                )
                .createdAt(review.getCreatedAt())
                .build();
    }
}
