package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.LikedPostListDto;
import Project.ProjectBackend.service.LikedPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class LikedPostController {

    private final LikedPostService likedPostService;

    // 좋아요 추가
    @PostMapping("/likes/add/{postNo}")
    public ResponseEntity<String> addLike(@PathVariable Long postNo, Authentication authentication) {
        String memberId = authentication.getName();
        likedPostService.addLike(memberId, postNo);
        return ResponseEntity.ok("게시글이 좋아요 목록에 추가되었습니다.");
    }

    // 좋아요 삭제
    @DeleteMapping("/likes/remove/{postNo}")
    public ResponseEntity<String> removeLike(@PathVariable Long postNo, Authentication authentication) {
        String memberId = authentication.getName();
        likedPostService.removeLike(memberId, postNo);
        return ResponseEntity.ok("게시글이 좋아요 목록에서 제거되었습니다.");
    }

    // 특정 게시글에 좋아요 여부 확인
    @GetMapping("/likes/check/{postNo}")
    public ResponseEntity<Boolean> isLiked(@PathVariable Long postNo, Authentication authentication) {
        String memberId = authentication.getName();
        boolean isLiked = likedPostService.isLiked(memberId, postNo);
        return ResponseEntity.ok(isLiked);
    }


    // 좋아요 된 횟수 카운트
    @GetMapping("/likes/count/{postNo}")
    public ResponseEntity<Long> countLiked(@PathVariable Long postNo) {
        long count = likedPostService.countLikesByPostNo(postNo);
        return ResponseEntity.ok(count);
    }

    // 로그인한 사용자의 좋아요한 게시글 목록 조회
    @GetMapping("/likes/list")
    public ResponseEntity<Slice<LikedPostListDto>> getLikedPosts(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        String memberId = authentication.getName();

        Sort sortOrder;
        switch (sortOption.toLowerCase()) {
            case "popular":
                sortOrder = Sort.by(Sort.Direction.DESC, "likeCount"); // 좋아요 수 기준 정렬
                break;
            case "latest":
            default:
                sortOrder = Sort.by(Sort.Direction.DESC, "createdAt"); // 좋아요한 날짜 기준 정렬
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<LikedPostListDto> likedPosts = likedPostService.getLikedPostsByMember(memberId, pageable);

        return ResponseEntity.ok(likedPosts);
    }
}
