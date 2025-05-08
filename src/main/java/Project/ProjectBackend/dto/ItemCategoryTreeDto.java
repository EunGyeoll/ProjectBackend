package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.ItemCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
// 트리구조 응답용 DTO
public class ItemCategoryTreeDto {

    private Long categoryId;

    @JsonProperty("categoryName")  // 👈 프론트에 맞게 맞춰줌
    private String categoryName;

    private List<ItemCategoryTreeDto> children = new ArrayList<>();

    public static ItemCategoryTreeDto from(ItemCategory category) {
        return new ItemCategoryTreeDto(
                category.getCategoryId(),
                category.getCategoryName(),
                category.getChildren().stream()
                        .map(ItemCategoryTreeDto::from)
                        .collect(Collectors.toList())
        );
    }
}