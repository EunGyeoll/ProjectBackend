package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.PostRequestDto;
import Project.ProjectBackend.dto.PostResponseDto;
import Project.ProjectBackend.service.PostService;
import Project.ProjectBackend.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final MemberService memberService;

    // 게시글 등록
    @PostMapping("/posts/new")
    public ResponseEntity<?> createBoard(@RequestBody @Valid PostRequestDto requestDto) {
        postService.createBoard(requestDto);
        return ResponseEntity.ok("게시글 등록 성공!");
    }

    // 게시글 목록 조회
    @GetMapping("/posts/list")
    public ResponseEntity<List<PostResponseDto>> getAllBoards() {
        List<PostResponseDto> boards = postService.getAllBoards();
        return ResponseEntity.ok(boards);
    }

    // 게시글 상세(단건) 조회
    @GetMapping("/posts/{postNo}")
        public ResponseEntity<PostResponseDto> getBoard(@PathVariable Long postNo) {
        PostResponseDto post = postService.getBoard(postNo);
        return ResponseEntity.ok(post);
    }

    // 게시글 삭제
    @DeleteMapping("/posts/{postNo}")
    public ResponseEntity<?> deleteBoard(@PathVariable Long postNo) {
        postService.deleteBoard(postNo);
        return ResponseEntity.ok("게시글 삭제 성공!");
    }
}
