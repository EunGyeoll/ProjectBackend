package Project.ProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class CategoryDto {
    private Long categoryId;
    private String name;
    private Long parentId;
    private List<CategoryDto> children = new ArrayList<>(); // 빈 리스트 초기화
}