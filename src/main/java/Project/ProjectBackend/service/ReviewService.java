package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.ReviewRequestDto;
import Project.ProjectBackend.dto.ReviewResponseDto;
import Project.ProjectBackend.entity.*;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.repository.ReviewRepository;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@Service
@AllArgsConstructor
public class ReviewService {
    private ReviewRepository reviewRepository;
    private MemberRepository memberRepository;
    private ImageService imageService;
    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);


    // 후기 작성
    @Transactional
    public Review writeReview(String writerId, String storeOwnerId, ReviewRequestDto requestDto, List<MultipartFile> imageFiles) {
        // 작성자 및 상점 소유자 조회
        Member writer = memberRepository.findByMemberId(writerId)
                .orElseThrow(() -> new IllegalArgumentException("작성자를 찾을 수 없습니다. ID: " + writerId));
        Member storeOwner = memberRepository.findByMemberId(storeOwnerId)
                .orElseThrow(() -> new IllegalArgumentException("상점 주인을 찾을 수 없습니다. ID: " + storeOwnerId));

        if (writerId.equals(storeOwnerId)) {
            throw new IllegalArgumentException("자신의 상점에는 리뷰를 작성할 수 없습니다.");
        }

        // 리뷰 생성 및 저장
        Review review = Review.builder()
                .writer(writer)
                .storeOwner(storeOwner)
                .content(requestDto.getContent())
                .rating(requestDto.getRating())
                .build();

        Review savedReview = reviewRepository.save(review);

        // 이미지 저장
        if (imageFiles != null && !imageFiles.isEmpty()) {
            logger.info("Saving images for review - reviewId: {}, imageCount: {}", savedReview.getReviewId(), imageFiles.size());
            List<Image> images = imageService.saveImagesForReview(imageFiles, savedReview);
            savedReview.setImages(images);

            if (!images.isEmpty()) {
                savedReview.setRepresentativeImagePath(images.get(0).getImagePath());
            }
        } else {
            logger.warn("No images provided for review - reviewId: {}", savedReview.getReviewId());
        }

        logger.info("Review created successfully - reviewId: {}", savedReview.getReviewId());
        return reviewRepository.save(savedReview);
    }




    // 후기 목록
    @Transactional(readOnly = true)
    public Slice<ReviewResponseDto> getReviewsForStore(String storeOwnerId, Pageable pageable) {

        return reviewRepository.findByStoreOwner_MemberIdOrderByCreatedAtDesc(storeOwnerId, pageable)
                .map(ReviewResponseDto::fromForList);
    }


    // 후기 단건 조회
    @Transactional(readOnly = true)
    public ReviewResponseDto getReviewByReviewId(Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new IllegalArgumentException("후기가 존재하지 않습니다."));

        return ReviewResponseDto.from(review);
    }


    // 리뷰 삭제
    @Transactional
    public void deleteReview(Long reviewId, String currentUserId) {
        // 리뷰 조회
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("삭제할 리뷰를 찾을 수 없습니다. ID: " + reviewId));


        // 현재 사용자 조회
        Member currentUser = memberRepository.findByMemberId(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("현재 사용자를 찾을 수 없습니다. ID: " + currentUserId));


        // 소유권 또는 권한 확인
        if (!review.getWriter().getMemberId().equals(currentUserId)  && !currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("리뷰를 삭제할 권한이 없습니다.");
        }

        // 연관된 이미지 삭제
        if (review.getImages() != null && !review.getImages().isEmpty()) {
            imageService.deleteImages(review.getImages());
        }

        // 리뷰 삭제
        reviewRepository.delete(review);
        logger.info("Review with ID {} deleted by user {}", reviewId, currentUserId);
    }
}
