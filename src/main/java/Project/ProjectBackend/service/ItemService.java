package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.ItemListDto;
import Project.ProjectBackend.entity.*;
import Project.ProjectBackend.dto.ItemRequestDto;
import Project.ProjectBackend.repository.CategoryRepository;
import Project.ProjectBackend.repository.ItemRepository;
import Project.ProjectBackend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final ImageService imageService;
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);


    // 1. 아이템 등록
    @Transactional
    public Item createItem(ItemRequestDto itemRequestDto, Member currentUser, List<MultipartFile> imageFiles) {
        logger.info("Creating item: {}", itemRequestDto.getItemName());

        ItemCategory category = categoryRepository.findById(itemRequestDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다."));

        // Item 엔티티 생성
        Item item = Item.builder()
                .seller(currentUser) // 현재 사용자로 설정
                .itemName(itemRequestDto.getItemName())
                .price(itemRequestDto.getPrice())
                .description(itemRequestDto.getDescription())
                .stockQuantity(itemRequestDto.getStockQuantity())
                .category(category)
                .build();

        // 아이템 먼저 저장하여 itemId 확보
        Item savedItem = itemRepository.save(item);
        logger.info("Item created with ID: {}", savedItem.getItemId());

        // 이미지 저장
        if (imageFiles != null && !imageFiles.isEmpty()) {
            logger.info("Saving {} images for item: {}", imageFiles.size(), item.getItemId());
            List<Image> images = imageService.saveImagesForItem(imageFiles, item);
            item.setImages(images);

            // 대표 이미지 설정
            if (!images.isEmpty()) {
                item.setRepresentativeImagePath(images.get(0).getImagePath());
            }

            // 이미지 설정 후 다시 저장
            savedItem = itemRepository.save(savedItem);
            logger.info("Item created with ID: {}", savedItem.getItemId());
        }

        return savedItem;
    }


    // 2. 아이템 수정
    @Transactional
    public Item updateItem(Long itemId, ItemRequestDto itemRequestDto, List<MultipartFile> imageFiles, Member currentUser) {
        // 1. 아이템 조회
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));

        // 2. 소유자 확인
        if (!existingItem.getSeller().equals(currentUser)) {
            throw new IllegalArgumentException("해당 아이템을 수정할 권한이 없습니다.");
        }

        // 3. 이미지 업데이트 처리
        if (imageFiles != null && !imageFiles.isEmpty()) {
            // 기존 이미지 삭제
            imageService.deleteImages(existingItem.getImages());

            // 새로운 이미지 저장
            List<Image> updatedImages = imageService.saveImagesForItem(imageFiles, existingItem);

            // 아이템에 새로운 이미지 설정
            existingItem.setImages(updatedImages);

            // 대표 이미지 설정
            if (!updatedImages.isEmpty()) {
                existingItem.setRepresentativeImagePath(updatedImages.get(0).getImagePath());
            }
        }
        // 새 이미지가 없는 경우 기존 이미지를 유지하므로 별도의 처리 불필요

        // 4. 나머지 속성 업데이트
        existingItem.setItemName(itemRequestDto.getItemName());
        existingItem.setPrice(itemRequestDto.getPrice());
        existingItem.setDescription(itemRequestDto.getDescription());
        existingItem.setStockQuantity(itemRequestDto.getStockQuantity());

        // 5. 아이템 저장
        return itemRepository.save(existingItem);
    }



    // 3. 아이템 단건(상세) 조회
    public Item getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템이 존재하지 않습니다."));

        // 조회수 증가
        item.increaseHitCount();
        itemRepository.save(item);


        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));
    }


    // 4. 특정 판매자가 등록한 아이템 조회
    @Transactional(readOnly = true)
    public Slice<ItemListDto> getItemsBySeller(String memberId, Pageable pageable) {
        Slice<Item> itemsSlice = itemRepository.findBySeller_MemberId(memberId, pageable);
        return itemsSlice.map(ItemListDto::from);
    }



    // 5. 모든 아이템 조회 (페이징 및 정렬 적용)
    public Slice<Item> getAllItems(Pageable pageable) {
        return itemRepository.findAll(pageable);
    }



    // 6. 키워드 검색 조회
    public Slice<Item> searchItemsByKeyword(String keyword, Pageable pageable) {

        Slice<Item> items = itemRepository.findByItemNameContainingIgnoreCase(
                keyword, pageable);

        return items;
    }



    // 7. 아이템 삭제
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));
        itemRepository.delete(item);
    }



//    private String extractFileNameFromPath(String path) {
//        // 예: "uploads/original/file.jpg" -> "file.jpg"
//        return path.substring(path.lastIndexOf("/") + 1);
//    }
//
//    private String generateUniqueFileName(String path) {
//        // 예: UUID를 기반으로 고유 파일 이름 생성
//        return UUID.randomUUID() + "_" + extractFileNameFromPath(path);
//    }
//
//    private long getFileSize(String path) {
//        File file = new File(path);
//        return file.exists() ? file.length() : 0L;
//    }




    }


