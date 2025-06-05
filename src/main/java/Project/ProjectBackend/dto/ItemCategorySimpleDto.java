package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.ItemCategory;
import lombok.Data;

@Data
public class ItemCategorySimpleDto {
    private Long categoryId;
    private String categoryName;

    public static ItemCategorySimpleDto from(ItemCategory entity) {
        ItemCategorySimpleDto dto = new ItemCategorySimpleDto();
        dto.setCategoryId(entity.getCategoryId());
        dto.setCategoryName(entity.getCategoryName());
        return dto;
    }
}
