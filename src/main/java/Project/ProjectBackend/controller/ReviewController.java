package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.ReviewRequestDto;
import Project.ProjectBackend.dto.ReviewResponseDto;
import Project.ProjectBackend.entity.Review;
import Project.ProjectBackend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ReviewController {

    private final ReviewService reviewService;

    private static final Logger logger = LoggerFactory.getLogger(ReviewController.class);

    // 1. 리뷰 작성 (타 회원의 상점인 storeOwnerId에 작성)
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/store/{storeOwnerId}/reviews")
    public ResponseEntity<ReviewResponseDto> writeReview(
            @PathVariable String storeOwnerId,
            @RequestPart("reviewData") @Valid ReviewRequestDto reviewRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
            Authentication authentication) {

        String writerId = authentication.getName(); // 현재 로그인 사용자 ID

        Review review = reviewService.writeReview(writerId, storeOwnerId, reviewRequestDto, imageFiles);
        logger.info("Review successfully created for storeOwnerId: {}", storeOwnerId);

        ReviewResponseDto reviewResponseDto = ReviewResponseDto.from(review); // DTO로 변환
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewResponseDto);
    }



    // 2. 리뷰 목록 조회
    @GetMapping("/store/{storeOwnerId}/reviews")
    public ResponseEntity<Slice<ReviewResponseDto>> getStoreReviews(
            @PathVariable String storeOwnerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        logger.info("getStoreReviews called - storeOwnerId: {}", storeOwnerId);

        Pageable pageable = PageRequest.of(page, size);
        Slice<ReviewResponseDto> reviews = reviewService.getReviewsForStore(storeOwnerId, pageable);

        logger.info("Retrieved {} reviews for storeOwnerId: {}", reviews.getNumberOfElements(), storeOwnerId);
        return ResponseEntity.ok(reviews);
    }

    // 3. 리뷰 단건 상세 조회
    @GetMapping("/store/reviews/{reviewId}")
    public ResponseEntity<ReviewResponseDto> getReviewDetail(@PathVariable Long reviewId) {

        ReviewResponseDto reviewResponse = reviewService.getReviewByReviewId(reviewId);

        return ResponseEntity.ok(reviewResponse);
    }


    // 4. 리뷰 삭제
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/reviews/{reviewId}")
    public ResponseEntity<Void> deleteReview(
            @PathVariable Long reviewId,
            Authentication authentication) {

        String currentUserId = authentication.getName(); // 현재 사용자 ID
        reviewService.deleteReview(reviewId, currentUserId);

        return ResponseEntity.noContent().build(); // 성공 응답 (204 No Content)
    }
}
