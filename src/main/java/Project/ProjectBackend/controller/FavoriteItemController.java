package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.ItemResponseDto;
import Project.ProjectBackend.service.FavoriteItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FavoriteItemController {

    private final FavoriteItemService favoriteItemService;

    // 찜 추가
    @PostMapping("/favorites/add/{itemId}")
    public ResponseEntity<String> addFavorite(@PathVariable Long itemId, Authentication authentication) {
        String memberId = authentication.getName(); // 현재 로그인한 사용자 ID
        favoriteItemService.addFavorite(memberId, itemId);
        return ResponseEntity.ok("아이템이 찜 목록에 추가되었습니다.");
    }

    // 찜 삭제
    @DeleteMapping("/favorites/remove/{itemId}")
    public ResponseEntity<String> removeFavorite(@PathVariable Long itemId, Authentication authentication) {
        String memberId = authentication.getName();
        favoriteItemService.removeFavorite(memberId, itemId);
        return ResponseEntity.ok("아이템이 찜 목록에서 제거되었습니다.");
    }

    //  찜 했는지 여부 체크
    @GetMapping("/favorites/check/{itemId}")
    public ResponseEntity<Boolean> isFavorite(@PathVariable Long itemId, Authentication authentication) {
        String memberId = authentication.getName();

        boolean isFavorite = favoriteItemService.isFavorite(memberId, itemId);

        return ResponseEntity.ok(isFavorite);
    }

    // 특정 사용자가 찜한 상품 목록
    @GetMapping("/favorites/list")
    public ResponseEntity<List<ItemResponseDto>> getFavorites(Authentication authentication) {
        String memberId = authentication.getName();
        List<ItemResponseDto> favorites = favoriteItemService.getFavorites(memberId);
        return ResponseEntity.ok(favorites);
    }
}
