package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ItemListDto {
    private Long itemId; // 상품 ID
    private String itemName;
    private Integer price;
    private Integer stockQuantity;
    private String sellerId;
    private String profileImageUrl;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime itemDate;
    private String representativeImagePath;


    public static ItemListDto from(Item item) {
        return ItemListDto.builder()
                .itemId(item.getItemId())
                .itemName(item.getItemName())
                .price(item.getPrice())
                .categoryId(item.getCategory() != null ? item.getCategory().getCategoryId() : null)
                .categoryName(item.getCategory() != null ? item.getCategory().getCategoryName() : null)
                .representativeImagePath(item.getImages().isEmpty() ? null : item.getImages().get(0).getImagePath())
                .itemDate(item.getItemDate())
                .build();
    }
}
