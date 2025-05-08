package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.PostCategoryDto;
import Project.ProjectBackend.entity.PostCategory;
import Project.ProjectBackend.repository.PostCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/post-categories")
@RequiredArgsConstructor
public class PostCategoryController {

    private final PostCategoryRepository postCategoryRepository;

    // 전체 조회
    @GetMapping
    public List<PostCategoryDto> getAll() {
        return postCategoryRepository.findAll().stream()
                .map(PostCategoryDto::from)
                .toList();
    }

    // 그룹별 카테고리 조회
    @GetMapping("/group/{groupName}")
    public List<PostCategoryDto> getByGroup(@PathVariable String groupName) {
        return postCategoryRepository.findByGroupNameOrderBySortOrderAsc(groupName).stream()
                .map(PostCategoryDto::from)
                .toList();
    }

    
    // 카테고리 정렬 순서 변경
    @PatchMapping("/admin/post-categories/{id}/sort")
    public ResponseEntity<?> updateSortOrder(@PathVariable Long id, @RequestParam Integer sortOrder) {
        PostCategory category = postCategoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("카테고리 없음"));
        category.setSortOrder(sortOrder);
        postCategoryRepository.save(category);
        return ResponseEntity.ok("정렬 순서 변경 완료");
    }

}
