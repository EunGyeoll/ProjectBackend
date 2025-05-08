package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.ItemCategory;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemCategoryFlatDto {
    private Long categoryId;
    private String categoryName;
    private Long parentId;

    public static ItemCategoryFlatDto from(ItemCategory category) {
        return new ItemCategoryFlatDto(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getParent() != null ? category.getParent().getCategoryId() : null
        );
    }
}
