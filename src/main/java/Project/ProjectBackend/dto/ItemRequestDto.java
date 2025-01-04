package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Image;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ItemRequestDto {
    private String sellerId;
    private String itemName;
    private Integer price;
    private String description;
    private Integer stockQuantity;
    private Long categoryId;
    private List<String> imagePaths;
    private List<String> originFileNames;
}