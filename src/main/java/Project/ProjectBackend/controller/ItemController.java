package Project.ProjectBackend.controller;

import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.dto.ItemRequestDto;
import Project.ProjectBackend.dto.ItemResponseDto;
import Project.ProjectBackend.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 모든 아이템 조회
    @GetMapping("/items/list")
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        List<ItemResponseDto> responseDtos = items.stream()
                .map(ItemResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }


    // 특정 판매자가 등록한 아이템 조회
    @GetMapping("/items/{sellerId}")
    public ResponseEntity<List<Item>> getItemsBySeller(@PathVariable String sellerId) {
        List<Item> items = itemService.getItemsBySeller(sellerId);
        return ResponseEntity.ok(items);
    }

    // 3. 아이템 등록
    @PostMapping("/items/new")
    public ResponseEntity<ItemResponseDto> createItem(@RequestBody ItemRequestDto itemRequestDto) {
        Item createdItem = itemService.createItem(itemRequestDto);
        ItemResponseDto responseDto = ItemResponseDto.from(createdItem);
        return ResponseEntity.ok(responseDto);
    }
//    @PostMapping("/items/new")
//    public ResponseEntity<Item> createItem(@RequestBody ItemRequestDto itemRequestDto) {
//        Item createdItem = itemService.createItem(itemRequestDto);
//        return ResponseEntity.ok(createdItem);
//    }

    // 4. 아이템 수정
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(@PathVariable Long itemId, @RequestBody ItemRequestDto updatedItemDto) {
        Item updatedItem = itemService.updateItem(itemId, updatedItemDto);
        ItemResponseDto responseDto = ItemResponseDto.from(updatedItem);
        return ResponseEntity.ok(responseDto);
    }



    // 5. 아이템 삭제
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<String> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok("삭제되었습니다!");
    }

}
