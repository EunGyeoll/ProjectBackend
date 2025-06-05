package Project.ProjectBackend.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class SearchResultDto {
    private List<ItemResponseDto> items;
    private List<PostResponseDto> posts;
    private boolean hasNext;
}
