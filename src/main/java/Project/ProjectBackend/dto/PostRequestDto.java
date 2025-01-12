package Project.ProjectBackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class PostRequestDto {
    private String writerId;
    private String title;
    private String content;
    private LocalDateTime boardDate;
    private List<String> imagePaths;
    private List<String> originFileNames;
}
