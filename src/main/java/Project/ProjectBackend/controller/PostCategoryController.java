package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.PostCategoryDto;
import Project.ProjectBackend.entity.PostCategory;
import Project.ProjectBackend.repository.PostCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/post-categories")
@RequiredArgsConstructor
public class PostCategoryController {

    private final PostCategoryRepository postCategoryRepository;

    // 전체 조회
    @GetMapping
    public ResponseEntity<List<PostCategoryDto>> getAll() {
        List<PostCategoryDto> categories = postCategoryRepository.findAll().stream()
                .map(PostCategoryDto::from)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(categories);
    }

    // 그룹별 카테고리 조회
    @GetMapping("/group/{groupName}")
    public List<PostCategoryDto> getByGroup(@PathVariable String groupName) {
        return postCategoryRepository.findByGroupNameOrderByCategoryNameAsc(groupName).stream()
                // 관리자가 지정한 순서로 조회하려면 findByGroupNameOrderBySortOrderAsc으로
                .map(PostCategoryDto::from)
                .toList();
    }



}
