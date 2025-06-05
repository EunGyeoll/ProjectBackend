package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.ItemCategorySimpleDto;
import Project.ProjectBackend.dto.ItemCategoryTreeDto;
import Project.ProjectBackend.entity.ItemCategory;
import Project.ProjectBackend.repository.ItemCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/item-categories")
@RequiredArgsConstructor
public class ItemCategoryController {
    private final ItemCategoryRepository itemCategoryRepository;

    // 모든 카테고리
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ItemCategorySimpleDto> getAllCategories() {
        return itemCategoryRepository.findAll()
                .stream()
                .map(ItemCategorySimpleDto::from)
                .toList();
    }


    // 트리 구조
    @GetMapping("/tree")
    public List<ItemCategoryTreeDto> getCategoryTree() {
        List<ItemCategory> roots = itemCategoryRepository.findByParentIsNull();
        return roots.stream().map(ItemCategoryTreeDto::from).toList();
    }

    // 최상위 카테고리
    @GetMapping("/top-level")
    public List<ItemCategory> getTopCategories() {
        return itemCategoryRepository.findByParentIsNull();
    }

    // 특정 부모의 자식 카테고리들
    @GetMapping("/{parentId}/children")
    public List<ItemCategory> getSubCategories(@PathVariable Long parentId) {
        return itemCategoryRepository.findByParentCategoryId(parentId);
    }

}
