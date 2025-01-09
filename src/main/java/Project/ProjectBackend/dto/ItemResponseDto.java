package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Category;
import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class ItemResponseDto {

    private Long itemId; // 상품 ID
    private String itemName;
    private Integer price;
    private String description;
    private Integer stockQuantity;
    private String sellerName;
    private String sellerEmail;
    private Long categoryId;
    private String categoryName;
    private String representativeImagePath;
    private List<String> imagePaths;

    // Item 엔티티를 기반으로 Dto 객체를 생성하는 메서드
    public static ItemResponseDto from(Item item) {
        Member seller = item.getSeller();
        Category category = item.getCategory();

        // 이미지 경로 리스트 생성
        List<String> imagePaths = item.getImages().stream()
                .map(Image::getImagePath)
                .collect(Collectors.toList());

        // 대표 이미지 경로 설정
        String representativeImagePath = imagePaths.isEmpty() ? null : imagePaths.get(0);

        return new ItemResponseDto(
                item.getItemId(),
                item.getItemName(),
                item.getPrice(),
                item.getDescription(),
                item.getStockQuantity(),
                seller != null ? seller.getName() : null,
                seller != null ? seller.getEmail() : null,
                category != null ? category.getCategoryId() : null,
                category != null ? category.getCategoryName() : null,
                representativeImagePath,
                imagePaths
        );
    }
}

