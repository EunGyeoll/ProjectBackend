package Project.ProjectBackend.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ItemRequestDto {

    @NotNull
    private String sellerId; // 판매자 ID

    @NotEmpty
    private String itemName; // 상품명

    @NotNull
    private Integer price; // 가격

    @NotEmpty
    private String description; // 상품 설명

    @NotNull
    private Integer stockQuantity; // 재고 수량

    private Long categoryId; // 카테고리 ID

}
