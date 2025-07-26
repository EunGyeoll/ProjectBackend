package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.PostListDto;
import Project.ProjectBackend.dto.PostRequestDto;
import Project.ProjectBackend.dto.PostResponseDto;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Post;
import Project.ProjectBackend.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class  PostController {

    private final PostService postService;
    private final FileService fileService;
    private final AuthService authService;
    private final ImageService imageService;
    private final SortService sortService;



    // 1. 게시글 등록
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/posts")
    public ResponseEntity<PostResponseDto> createPost(@RequestBody @Valid PostRequestDto postRequestDto) {
        Member currentUser = authService.getCurrentUser();
        Post createdPost = postService.createPost(postRequestDto, currentUser);

        return ResponseEntity.ok(PostResponseDto.from(createdPost));
    }

    // 2. 게시글 수정
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postId,
            @RequestPart("postData") @Valid PostRequestDto postRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        Member currentUser = authService.getCurrentUser(); // 현재 로그인된 사용자

        PostResponseDto updatedPost = PostResponseDto.from(postService.updatePost(postId, postRequestDto, imageFiles, currentUser));

        return ResponseEntity.ok(updatedPost);
    }


    // 3. 게시글 상세(단건) 조회
    @GetMapping("/posts/{postId}")
    public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postId) {
        Member currentUser = authService.getCurrentUserOrNull();

        // 로그인되지 않았다면 null 반환
        String currentUserId = (currentUser != null) ? currentUser.getMemberId() : null;

        PostResponseDto responseDto = postService.getPost(postId, currentUserId);

        return ResponseEntity.ok(responseDto);
    }



    // 4. 모든 게시글 목록 조회
    @GetMapping("/posts/list")
    public ResponseEntity<Slice<PostResponseDto>> getAllPosts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

           Sort sortOrder = sortService.createSort(sortOption, "post");
           Pageable pageable = PageRequest.of(page, size, sortOrder);

           Slice<Post> postSlice;

            if (categoryId != null) {
                postSlice = postService.getPostsByCategoryId(categoryId, pageable);
            } else {
                postSlice = postService.getAllPosts(pageable);
            }

           Slice<PostResponseDto> postDtoSlice = postSlice.map(PostResponseDto::from);

            return ResponseEntity.ok(postDtoSlice);
    }

    // 5. 카테고리별 게시글 목록 조회
//    @GetMapping("/posts/category/{categoryId}")
//    public ResponseEntity<Slice<PostListDto>> getPostsByCategory(
//            @PathVariable Long categoryId,
//            @RequestParam(defaultValue = "0") int page,
//            @RequestParam(defaultValue = "10") int size,
//            @RequestParam(defaultValue = "latest") String sortOption) { // ✅ sortOption 받기
//
//        Sort sortOrder = sortService.createSort(sortOption, "post");
//        Pageable pageable = PageRequest.of(page, size, sortOrder);
//
//        Slice<PostListDto> posts = postService.getPostsByCategory(categoryId, pageable);
//        return ResponseEntity.ok(posts);
//    }
//

    // 6. 게시글 특정 멤버별 조회
    @GetMapping("/posts/writer/{memberId}")
    public ResponseEntity<Slice<PostResponseDto>> getPostsBySeller(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) { // sortOption 추가

        Sort sortOrder = sortService.createSort(sortOption, "post");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        Slice<Post> postSlice = postService.getAllPosts(pageable);
        Slice<PostResponseDto> postDtoSlice = postSlice.map(PostResponseDto::from);

        return ResponseEntity.ok(postDtoSlice);

    }


    // 7. 게시글 삭제
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @DeleteMapping("/posts/{postNo}")
    public ResponseEntity<?> deletePost(@PathVariable Long postNo) {
        postService.deletePost(postNo);


        return ResponseEntity.ok("게시글 삭제 성공!");
    }
}
