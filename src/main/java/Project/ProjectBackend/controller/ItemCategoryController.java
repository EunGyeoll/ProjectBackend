package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.ItemCategoryTreeDto;
import Project.ProjectBackend.entity.ItemCategory;
import Project.ProjectBackend.repository.ItemCategoryRepository;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/tree")
    public List<ItemCategoryTreeDto> getCategoryTree() {
        List<ItemCategory> roots = itemCategoryRepository.findByParentIsNull();
        return roots.stream().map(ItemCategoryTreeDto::from).toList();
    }

    @GetMapping
    public List<ItemCategory> getAllCategories() {
        return itemCategoryRepository.findAll();
    }

    @GetMapping("/top-level")
    public List<ItemCategory> getTopCategories() {
        return itemCategoryRepository.findByParentIsNull();
    }

    @GetMapping("/{parentId}/children")
    public List<ItemCategory> getSubCategories(@PathVariable Long parentId) {
        return itemCategoryRepository.findByParentCategoryId(parentId);
    }

}
