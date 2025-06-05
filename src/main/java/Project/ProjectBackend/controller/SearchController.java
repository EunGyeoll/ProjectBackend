package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.ItemResponseDto;
import Project.ProjectBackend.dto.PostResponseDto;
import Project.ProjectBackend.dto.SearchResultDto;
import Project.ProjectBackend.entity.Item;
import Project.ProjectBackend.entity.Post;
import Project.ProjectBackend.service.ItemService;
import Project.ProjectBackend.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/search")
public class SearchController {

    private final ItemService itemService;
    private final PostService postService;

    @GetMapping
    public SearchResultDto searchAll(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(required = false) String itemCategory,
            @RequestParam(required = false) String postCategory
    ) {

        Pageable itemPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "itemDate"));
        Pageable postPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "postDate"));

       Slice<Item> items = (itemCategory != null && !itemCategory.isEmpty())
            ? itemService.searchItemsByKeywordAndCategory(keyword, itemCategory, itemPageable)
            : itemService.searchItemsByKeyword(keyword, itemPageable);

        // 게시글 검색
        Slice<Post> posts = (postCategory != null && !postCategory.isEmpty())
                ? postService.searchPostsByKeywordAndCategory(keyword, postCategory, postPageable)
                : postService.searchPostsByKeyword(keyword, postPageable);

        return SearchResultDto.builder()
                .items(items.map(ItemResponseDto::fromForList).getContent())
                .posts(posts.map(PostResponseDto::fromForList).getContent())
                .hasNext(items.hasNext())
                .hasNext(posts.hasNext())
                .build();
    }

}
