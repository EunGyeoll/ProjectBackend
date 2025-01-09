package Project.ProjectBackend.service;

import Project.ProjectBackend.entity.Category;
import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.dto.ItemRequestDto;
import Project.ProjectBackend.repository.CategoryRepository;
import Project.ProjectBackend.repository.ItemRepository;
import Project.ProjectBackend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private final ItemRepository itemRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;

    // 모든 아이템 조회
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    // 특정 판매자가 등록한 아이템 조회
    public List<Item> getItemsBySeller(String sellerId) {
        Member seller = memberRepository.findByMemberId(sellerId)
                .orElseThrow(()-> new IllegalArgumentException("판매자를 찾을 수 없습니다."));
        return itemRepository.findBySeller(seller);
    }

    // 아이템 등록
    public Item createItem(ItemRequestDto itemRequestDto, Member currentUser) {
        // 작성자(Member) 조회
        Member seller = memberRepository.findById(itemRequestDto.getSellerId())
                .orElseThrow(() -> new IllegalArgumentException("판매자가 존재하지 않습니다."));

        Category category = categoryRepository.findById(itemRequestDto.getCategoryId())
                .orElseThrow(()-> new IllegalArgumentException("유효하지 않은 카테고리입니다."));

        // Item 엔티티 생성
        Item item = Item.builder()
//                .seller(seller)
                .seller(currentUser)
                .itemName(itemRequestDto.getItemName())
                .price(itemRequestDto.getPrice())
                .description(itemRequestDto.getDescription())
                .stockQuantity(itemRequestDto.getStockQuantity())
                .category(category) // Category 설정
                .build(); // 날짜는 @CreationTimeStamp 가 붙어있는 필드는 Hibernate를 통해 자동으로 값이 db에 들어감.

        // 이미지 저장
        List<Image> images = itemRequestDto.getImagePaths().stream()
                .map(path -> Image.builder()
                        .imagePath(path)
                        .originFileName(extractFileNameFromPath(path)) // 파일 경로에서 원본 파일 이름 추출
                        .newFileName(generateUniqueFileName(path)) // 저장된 파일 이름 생성
                        .fileSize(getFileSize(path)) // 파일 크기 정보
                        .item(item) // 연관관계 설정
                        .build())
                .collect(Collectors.toList());
        item.setImages(images);

        // 대표 이미지 설정
        if (!images.isEmpty()) {
            item.setRepresentativeImagePath(images.get(0).getImagePath());
        }

        return itemRepository.save(item);
    }

    private String extractFileNameFromPath(String path) {
        // 예: "uploads/original/file.jpg" -> "file.jpg"
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private String generateUniqueFileName(String path) {
        // 예: UUID를 기반으로 고유 파일 이름 생성
        return UUID.randomUUID() + "_" + extractFileNameFromPath(path);
    }

    private long getFileSize(String path) {
        // 파일 경로를 사용해 실제 파일 크기를 가져오는 로직 구현 필요
        File file = new File(path);
        return file.exists() ? file.length() : 0L;
    }


    // 아이템 수정
    public Item updateItem(Long itemId, ItemRequestDto updatedItemDto, Member currentUser) {
        Item existingItem = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));

        // 현재 사용자가 아이템의 소유자인지 확인
        if (!existingItem.getSeller().equals(currentUser)) {
            throw new SecurityException("해당 아이템을 수정할 권한이 없습니다.");
        }

        // 이미지 업데이트 처리
        if (updatedItemDto.getImagePaths() != null && !updatedItemDto.getImagePaths().isEmpty()) {
            List<Image> updatedImages = updatedItemDto.getImagePaths().stream()
                    .map(path -> Image.builder()
                            .imagePath(path)
                            .item(existingItem) // 연관 관계 설정
                            .build())
                    .collect(Collectors.toList());
            existingItem.setImages(updatedImages);

            // 대표 이미지 설정
            existingItem.setRepresentativeImagePath(updatedImages.get(0).getImagePath());
        } else {
            // 이미지가 없는 경우 기존 이미지를 유지
            existingItem.setImages(existingItem.getImages());
        }

        // 나머지 속성 업데이트
        existingItem.setItemName(updatedItemDto.getItemName());
        existingItem.setPrice(updatedItemDto.getPrice());
        existingItem.setDescription(updatedItemDto.getDescription());
        existingItem.setStockQuantity(updatedItemDto.getStockQuantity());

        return itemRepository.save(existingItem);
    }




    public List<Image> getExistingImages(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));
        return item.getImages();
    }

    // 아이템 삭제
    public void deleteItem(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("아이템을 찾을 수 없습니다."));
        itemRepository.delete(item);
    }


    }


