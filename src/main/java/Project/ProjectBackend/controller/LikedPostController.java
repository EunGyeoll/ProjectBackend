package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.PostResponseDto;
import Project.ProjectBackend.service.LikedPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
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

    // 로그인한 사용자의 좋아요한 게시글 목록 조회
    @GetMapping("/likes/list")
    public ResponseEntity<List<PostResponseDto>> getLikedPosts(Authentication authentication) {
        String memberId = authentication.getName();
        List<PostResponseDto> likes = likedPostService.getLikedPosts(memberId);
        return ResponseEntity.ok(likes);
    }
}
