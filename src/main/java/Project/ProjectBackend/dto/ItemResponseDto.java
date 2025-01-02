package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Category;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ItemResponseDto {

    private Long itemId; // 상품 ID
    private String itemName; // 상품명
    private Integer price; // 가격
    private String description; // 상품 설명
    private Integer stockQuantity; // 재고 수량
    private String sellerName; // 판매자 이름
    private String sellerEmail; // 판매자 이메일
    private String categoryName; // 카테고리명

    // Item 엔티티를 기반으로 Dto 객체를 생성하는 메서드
    public static ItemResponseDto from(Item item) {
        Member seller = item.getSeller();
        Category category = item.getCategory();

        return new ItemResponseDto(
                item.getItemId(),
                item.getItemName(),
                item.getPrice(),
                item.getDescription(),
                item.getStockQuantity(),
                seller != null ? seller.getName() : null, // 판매자 이름
                seller != null ? seller.getEmail() : null, // 판매자 이메일
                category != null ? category.getCategoryName() : null // 카테고리 이름
        );
    }
}
