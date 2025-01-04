package Project.ProjectBackend.controller;


import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.dto.ItemRequestDto;
import Project.ProjectBackend.dto.ItemResponseDto;
import Project.ProjectBackend.service.FileService;
import Project.ProjectBackend.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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

    // 1. 모든 아이템 조회
    @GetMapping("/items/list")
    public ResponseEntity<List<ItemResponseDto>> getAllItems() {
        List<Item> items = itemService.getAllItems();
        List<ItemResponseDto> responseDtos = items.stream()
                .map(ItemResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    // 2. 특정 판매자가 등록한 아이템 조회
    @GetMapping("/items/{sellerId}")
    public ResponseEntity<List<ItemResponseDto>> getItemsBySeller(@PathVariable String sellerId) {
        List<Item> items = itemService.getItemsBySeller(sellerId);
        List<ItemResponseDto> responseDtos = items.stream()
                .map(ItemResponseDto::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseDtos);
    }

    // 3. 아이템 등록
    @PostMapping("/items/new")
    public ResponseEntity<ItemResponseDto> createItem(
            @RequestPart(value = "itemData") ItemRequestDto itemRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        List<Image> images = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String filePath = fileService.saveFile(file); // 파일 저장 후 경로 반환
                Image image = new Image();
                image.setOriginFileName(file.getOriginalFilename()); // 원본 파일 이름 설정
                image.setNewFileName(filePath.substring(filePath.lastIndexOf("/") + 1)); // 서버 저장 파일 이름
                image.setImagePath(filePath); // 파일 경로
                image.setFileSize(file.getSize()); // 파일 크기
                images.add(image);
            }
        }

        itemRequestDto.setImagePaths(images.stream().map(Image::getImagePath).collect(Collectors.toList()));

        Item createdItem = itemService.createItem(itemRequestDto);
        return ResponseEntity.ok(ItemResponseDto.from(createdItem));
    }


    // 4. 아이템 수정
    @PutMapping("/items/{itemId}")
    public ResponseEntity<ItemResponseDto> updateItem(
            @PathVariable Long itemId,
            @RequestPart("itemData") ItemRequestDto updatedItemDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        List<Image> images = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String filePath = fileService.saveFile(file); // 파일 저장 후 경로 반환
                Image image = new Image();
                image.setOriginFileName(file.getOriginalFilename()); // 원본 파일 이름 설정
                image.setNewFileName(file.getOriginalFilename()); // 원본 파일 이름 그대로 설정
                image.setImagePath(filePath); // 파일 경로
                image.setFileSize(file.getSize()); // 파일 크기
                images.add(image);
            }
            updatedItemDto.setImagePaths(images.stream().map(Image::getImagePath).collect(Collectors.toList()));
        } else {
            // 새 이미지가 없는 경우 기존 이미지 유지
            List<Image> existingImages = itemService.getExistingImages(itemId);

            updatedItemDto.setImagePaths(existingImages.stream()
                    .map(Image::getImagePath)
                    .collect(Collectors.toList()));
            updatedItemDto.setOriginFileNames(existingImages.stream()
                    .map(Image::getOriginFileName)
                    .collect(Collectors.toList()));
        }

        // 서비스 계층 호출
        Item updatedItem = itemService.updateItem(itemId, updatedItemDto);

        return ResponseEntity.ok(ItemResponseDto.from(updatedItem));
    }




    // 5. 아이템 삭제
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<String> deleteItem(@PathVariable Long itemId) {
        itemService.deleteItem(itemId);
        return ResponseEntity.ok("삭제되었습니다!");
    }
}
