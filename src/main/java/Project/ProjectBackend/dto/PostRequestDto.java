package Project.ProjectBackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class PostRequestDto {
    private String writerId; // 이 반환 타입을 String 으로 할지 Member 타입으로 할지 고민이다.
    private String title;
    private String content;
    private LocalDateTime boardDate;
        private List<String> imagePaths;
    private List<String> originFileNames;
}
