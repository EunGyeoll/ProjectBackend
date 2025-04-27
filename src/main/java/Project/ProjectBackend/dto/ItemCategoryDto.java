package Project.ProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class ItemCategoryDto {
    private Long categoryId;
    private String name;
    private Long parentId;
    private List<ItemCategoryDto> children = new ArrayList<>(); // 빈 리스트 초기화
}