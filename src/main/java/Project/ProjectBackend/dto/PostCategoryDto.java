package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.PostCategory;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostCategoryDto {

    private Long id;           // post_category_id

    @JsonProperty("categoryName")
    private String categoryName;       // category_name

    @JsonProperty("groupName")
    private String groupName;          // 커뮤니티, 큐레이팅 등 분류 그룹명

    // 엔티티 -> DTO 변환 메서드
    public static PostCategoryDto from(PostCategory postCategory) {
        return PostCategoryDto.builder()
                .id(postCategory.getCategoryId())
                .categoryName(postCategory.getCategoryName())
                .groupName(postCategory.getGroupName())
                .build();
    }
}
