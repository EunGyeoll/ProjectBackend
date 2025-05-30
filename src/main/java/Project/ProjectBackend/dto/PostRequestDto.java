package Project.ProjectBackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class PostRequestDto {
    private String title;
    private String content;
    private LocalDateTime postDate;
    private Long categoryId;
//    private List<String> imagePaths;
//    private List<String> originFileNames;
}
