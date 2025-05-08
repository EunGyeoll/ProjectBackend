package Project.ProjectBackend.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategoryRequestDto {
    @NotEmpty(message = "카테고리 이름은 필수입니다.")
    private String name;

    private Long parentId; // null이면 최상위 카테고리로 등록
}
