package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.PostResponseDto;
import Project.ProjectBackend.service.LikedPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class LikedPostController {

    private final LikedPostService likedPostService;

    // 좋아요 추가
    @PostMapping("/likes/add/{memberId}/{postNo}")
    public ResponseEntity<String> addFavorite(@PathVariable String memberId, @PathVariable Long postNo) {
        likedPostService.addLike(memberId, postNo);
        return ResponseEntity.ok("게시글이 좋아요 목록에 추가되었습니다.");
    }

    // 좋아요 삭제
    @DeleteMapping("/likes/remove/{memberId}/{postNo}")
    public ResponseEntity<String> removeFavorite(@PathVariable String memberId, @PathVariable Long postNo) {
        likedPostService.removeLike(memberId, postNo);
        return ResponseEntity.ok("게시글이 좋아요 목록에서 제거되었습니다.");
    }

    // 좋아요 했는지 여부 체크
    @GetMapping("/likes/check/{memberId}/{postNo}")
    public ResponseEntity<Boolean> isFavorite(@PathVariable String memberId, @PathVariable Long postNo) {
        boolean isLiked = likedPostService.isLiked(memberId, postNo);
        return ResponseEntity.ok(isLiked);
    }


    // 특정 회원의 좋아요 목록
    @GetMapping("/likes/list/{memberId}")
    public ResponseEntity<List<PostResponseDto>> getFavorites(@PathVariable String memberId) {
        List<PostResponseDto> likes = likedPostService.getLikedPosts(memberId);
        return ResponseEntity.ok(likes);
    }

}
