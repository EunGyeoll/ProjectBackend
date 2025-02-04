package Project.ProjectBackend.controller;


import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.dto.ItemRequestDto;
import Project.ProjectBackend.dto.ItemResponseDto;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.service.AuthService;
import Project.ProjectBackend.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final AuthService authService;


    // 1. 아이템 등록
    @PreAuthorize("hasAuthority('ROLE_USER')")
    @PostMapping("/items/new")
    public ResponseEntity<ItemResponseDto> createItem(
            @RequestPart(value = "itemData") @Valid ItemRequestDto itemRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        Member currentUser = authService.getCurrentUser(); // 현재 로그인된 사용자

        Item createdItem = itemService.createItem(itemRequestDto, currentUser, imageFiles); // 이미지 파일 전달
        return ResponseEntity.ok(ItemResponseDto.from(createdItem));
    }



    // 2. 아이템 수정
    @PreAuthorize("hasAuthority('ROLE_USER')")
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




    // 3. 아이템 상세(단건) 조회
    @GetMapping("/items/{itemId}")
    public ResponseEntity<ItemResponseDto> getItem(@PathVariable Long itemId) {
        Item item = itemService.getItemById(itemId);
        ItemResponseDto responseDto = ItemResponseDto.from(item);
        return ResponseEntity.ok(responseDto);
    }


    // 4. 모든 아이템 목록 조회
    @GetMapping("/items/list")
    public ResponseEntity<Slice<ItemResponseDto>> getAllItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder;

        switch (sortOption.toLowerCase()) {
            case "popular":
                sortOrder = Sort.by(Sort.Direction.DESC, "favoriteCount");
                break;
            case "lowprice":
                sortOrder = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "highprice":
                sortOrder = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "latest":
            default:
                sortOrder = Sort.by(Sort.Direction.DESC, "itemDate");
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<Item> itemsSlice = itemService.getAllItems(pageable);
        Slice<ItemResponseDto> responseDtosSlice = itemsSlice.map(ItemResponseDto::fromForList);

        return ResponseEntity.ok(responseDtosSlice);
    }



    // 5. 특정 판매자가 등록한 아이템 조회
    @GetMapping("/items/seller/{memberId}")
    public ResponseEntity<Slice<ItemResponseDto>> getItemsBySeller(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder;

        switch (sortOption.toLowerCase()) {
            case "popular":
                sortOrder = Sort.by(Sort.Direction.DESC, "favoriteCount");
                break;
            case "lowprice":
                sortOrder = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "highprice":
                sortOrder = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "latest":
            default:
                sortOrder = Sort.by(Sort.Direction.DESC, "itemDate");
                break;
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        // ✅ ItemService에서 이미 DTO 변환된 Slice<ItemResponseDto> 반환
        Slice<ItemResponseDto> itemsSlice = itemService.getItemsBySeller(memberId, pageable);

        return ResponseEntity.ok(itemsSlice);
    }




    // 6. 키워드 검색으로 조회
    @GetMapping("/items/search")
    public ResponseEntity<Slice<ItemResponseDto>> searchItems(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Sort sortOrder;

        switch (sortOption.toLowerCase()) {
            case "popular": // 인기순. 찜 수가 많은 순으로
                sortOrder = Sort.by(Sort.Direction.DESC, "favoriteCount");
                break;
            case "lowprice": // 가격 낮은 순.
                sortOrder = Sort.by(Sort.Direction.ASC, "price");
                break;
            case "highprice":
                sortOrder = Sort.by(Sort.Direction.DESC, "price");
                break;
            case "latest": // 최신순.
            default:
                sortOrder = Sort.by(Sort.Direction.DESC, "itemDate");
                break;
        }

        Pageable pageable = PageRequest.of(page,size,sortOrder);

        Slice<Item> itemSlice = itemService.searchItemsByKeyword(keyword,pageable);

        // 엔티티를 Dto로 변환
        Slice<ItemResponseDto> responseDtoSlice = itemSlice.map(ItemResponseDto::fromForList);

        return ResponseEntity.ok(responseDtoSlice);

    }



    // 7. 아이템 삭제
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<String> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok("삭제되었습니다!");
    }
}
