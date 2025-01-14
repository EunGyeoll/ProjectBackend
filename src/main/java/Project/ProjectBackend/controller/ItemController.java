package Project.ProjectBackend.controller;


import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.dto.ItemRequestDto;
import Project.ProjectBackend.dto.ItemResponseDto;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.service.AuthService;
import Project.ProjectBackend.service.FileService;
import Project.ProjectBackend.service.ImageService;
import Project.ProjectBackend.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final FileService fileService;
    private final AuthService authService;
    private final ImageService imageService;


    // 1. 모든 아이템 조회
    @GetMapping("items/list")
    public ResponseEntity<Slice<ItemResponseDto>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "itemDate,desc") String[] sort) {

        // 정렬
        Sort.Direction direction = Sort.Direction.fromString(sort[1]); // "desc" → Sort.Direction.DESC
        Sort sortOrder = Sort.by(direction, sort[0]); // itemDate 필드를 기준으로 내림차순 정렬
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<Item> itemsSlice = itemService.getAllItems(pageable);
        Slice<ItemResponseDto> responseDtosSlice = itemsSlice.map(ItemResponseDto::from);

        return ResponseEntity.ok(responseDtosSlice);
    }

//    @GetMapping("/items/list")
//    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
//        List<Item> items = itemService.getAllItems();
//        List<ItemResponseDto> responseDtos = items.stream()
//                .map(ItemResponseDto::from)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(responseDtos);
//    }

    // 2. 특정 판매자가 등록한 아이템 조회
    @GetMapping("/items/seller/{memberId}")
    public ResponseEntity<Slice<ItemResponseDto>> getItemsBySeller(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "itemDate,desc") String[] sort) {

        // 정렬 처리
        Sort.Direction direction = Sort.Direction.fromString(sort[1]);
        Sort sortOrder = Sort.by(direction, sort[0]);
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<Item> itemsSlice = itemService.getItemsBySeller(memberId, pageable);
        Slice<ItemResponseDto> responseDtosSlice = itemsSlice.map(ItemResponseDto::from);

        return ResponseEntity.ok(responseDtosSlice);
    }

//    @GetMapping("/items/{sellerId}")
//    public ResponseEntity<List<ItemResponseDto>> getItemsBySeller(@PathVariable String sellerId) {
//        List<Item> items = itemService.getItemsBySeller(sellerId);
//        List<ItemResponseDto> responseDtos = items.stream()
//                .map(ItemResponseDto::from)
//                .collect(Collectors.toList());
//        return ResponseEntity.ok(responseDtos);
//    }

    // 3. 아이템 등록
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/items/new")
    public ResponseEntity<ItemResponseDto> createItem(
            @RequestPart(value = "itemData") @Valid ItemRequestDto itemRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        Member currentUser = authService.getCurrentUser(); // 현재 로그인된 사용자

        Item createdItem = itemService.createItem(itemRequestDto, currentUser, imageFiles); // 이미지 파일 전달
        return ResponseEntity.ok(ItemResponseDto.from(createdItem));
    }


    // 4. 아이템 수정
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(
            @PathVariable Long itemId,
            @RequestPart("itemData") @Valid ItemRequestDto itemRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        // 현재 로그인된 사용자 가져오기
        Member currentUser = authService.getCurrentUser();

        // 서비스 계층 호출, 이미지 파일 전달
        Item updatedItem = itemService.updateItem(itemId, itemRequestDto, imageFiles, currentUser);

        return ResponseEntity.ok(ItemResponseDto.from(updatedItem));
    }



    // 5. 아이템 삭제
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<String> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok("삭제되었습니다!");
    }
}
