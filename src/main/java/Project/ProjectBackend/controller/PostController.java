package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.PostRequestDto;
import Project.ProjectBackend.dto.PostResponseDto;
import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Post;
import Project.ProjectBackend.service.FileService;
import Project.ProjectBackend.service.PostService;
import Project.ProjectBackend.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MemberService memberService;
    private final FileService fileService;

    // 게시글 등록
    @PostMapping("/posts/new")
    public ResponseEntity<PostResponseDto> createItem(
            @RequestPart(value = "postData") PostRequestDto postRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        List<Image> images = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String filePath = fileService.saveFile(file); // 파일 저장 후 경로 반환
                Image image = new Image();
                image.setOriginFileName(file.getOriginalFilename()); // 원본 파일 이름 설정
                image.setNewFileName(filePath.substring(filePath.lastIndexOf("/") + 1)); // 서버 저장 파일 이름
                image.setImagePath(filePath); // 파일 경로
                image.setFileSize(file.getSize()); // 파일 크기
                images.add(image);
            }
        }

        postRequestDto.setImagePaths(images.stream().map(Image::getImagePath).collect(Collectors.toList()));

        Post createdPost = postService.createPost(postRequestDto);
        return ResponseEntity.ok(PostResponseDto.from(createdPost));
    }
//    public ResponseEntity<?> createBoard(@RequestBody @Valid PostRequestDto requestDto) {
//        postService.createBoard(requestDto);
//        return ResponseEntity.ok("게시글 등록 성공!");
//    }

    // 게시글 수정
    @PutMapping("/posts/{postNo}")
    public ResponseEntity<PostResponseDto> updatePost(
            @PathVariable Long postNo,
            @RequestPart(value = "postData") PostRequestDto postRequestDto,
            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> imageFiles) {

        List<Image> images;
        if (imageFiles != null && !imageFiles.isEmpty()) {
            images = saveImages(imageFiles);
            postRequestDto.setImagePaths(images.stream().map(Image::getImagePath).collect(Collectors.toList()));
        } else {
            // 새 이미지가 없는 경우 기존 이미지 유지
            List<Image> existingImages = postService.getExistingImages(postNo);
            postRequestDto.setImagePaths(existingImages.stream().map(Image::getImagePath).collect(Collectors.toList()));
        }

        PostResponseDto updatedPost = postService.updatePost(postNo, postRequestDto);
        return ResponseEntity.ok(updatedPost); // 수정된 부분
    }

    // 이미지 저장 처리 메서드
    private List<Image> saveImages(List<MultipartFile> imageFiles) {
        List<Image> images = new ArrayList<>();
        if (imageFiles != null && !imageFiles.isEmpty()) {
            for (MultipartFile file : imageFiles) {
                String filePath = fileService.saveFile(file); // 파일 저장 후 경로 반환
                Image image = new Image();
                image.setOriginFileName(file.getOriginalFilename()); // 원본 파일 이름 설정
                image.setNewFileName(filePath.substring(filePath.lastIndexOf("/") + 1)); // 서버 저장 파일 이름
                image.setImagePath(filePath); // 파일 경로
                image.setFileSize(file.getSize()); // 파일 크기
                images.add(image);
            }
        }
        return images;
    }


    // 게시글 목록 조회
    @GetMapping("/posts/list")
    public ResponseEntity<List<PostResponseDto>> getAllPosts() {
        List<PostResponseDto> boards = postService.getAllPosts();
        return ResponseEntity.ok(boards);
    }

    // 게시글 상세(단건) 조회
    @GetMapping("/posts/{postNo}")
        public ResponseEntity<PostResponseDto> getPost(@PathVariable Long postNo) {
        PostResponseDto post = postService.getPost(postNo);
        return ResponseEntity.ok(post);
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{postNo}")
    public ResponseEntity<?> deletePost(@PathVariable Long postNo) {
        postService.deletePost(postNo);
        return ResponseEntity.ok("게시글 삭제 성공!");
    }
}
