package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.ItemResponseDto;
import Project.ProjectBackend.service.FavoriteItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FavoriteItemController {

    private final FavoriteItemService favoriteItemService;

    // 찜 추가
    @PostMapping("/favorites/add/{memberId}/{itemId}")
    public ResponseEntity<String> addFavorite(@PathVariable String memberId, @PathVariable Long itemId) {
        favoriteItemService.addFavorite(memberId, itemId);
        return ResponseEntity.ok("아이템이 찜 목록에 추가되었습니다.");
    }

    // 찜 삭제
    @DeleteMapping("/favorites/remove/{memberId}/{itemId}")
    public ResponseEntity<String> removeFavorite(@PathVariable String memberId, @PathVariable Long itemId) {
        favoriteItemService.removeFavorite(memberId, itemId);
        return ResponseEntity.ok("아이템이 찜 목록에서 제거되었습니다.");
    }

    // 찜 했는지 여부 체크
    @GetMapping("/favorites/check/{memberId}/{itemId}")
    public ResponseEntity<Boolean> isFavorite(@PathVariable String memberId, @PathVariable Long itemId) {
        boolean isFavorite = favoriteItemService.isFavorite(memberId, itemId);
        return ResponseEntity.ok(isFavorite);
    }


    // 특정 회원의 찜 목록
    @GetMapping("/favorites/list/{memberId}")
    public ResponseEntity<List<ItemResponseDto>> getFavorites(@PathVariable String memberId) {
        List<ItemResponseDto> favorites = favoriteItemService.getFavorites(memberId);
        return ResponseEntity.ok(favorites);
    }
}
