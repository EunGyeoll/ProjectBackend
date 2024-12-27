package Project.ProjectBackend.controller;

import Project.ProjectBackend.domain.Comment;
import Project.ProjectBackend.domain.Member;
import Project.ProjectBackend.dto.CommentRequestDto;
import Project.ProjectBackend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/{postNo}")
    public ResponseEntity<String> addComment(
            @PathVariable Long postNo,
            @RequestBody CommentRequestDto requestDto
    ) {
        commentService.addComment(postNo, requestDto);
        return ResponseEntity.ok("댓글이 성공적으로 추가되었습니다.");
    }

    // 대댓글 작성
    @PostMapping("/{postNo}/reply/{parentCommentId}")
    public ResponseEntity<Comment> createReply(
            @PathVariable Long postNo,
            @PathVariable Long parentCommentId,
            @RequestBody CommentRequestDto request) {
        Member writer = new Member(); // 작성자 정보를 설정해야 함
        Comment reply = commentService.createReply(postNo, parentCommentId, writer, request.getContent());
        return ResponseEntity.ok(reply);
    }

    // 댓글 조회
    @GetMapping("/{postNo}")
    public ResponseEntity<List<Comment>> getCommentsByPost(@PathVariable Long postNo) {
        List<Comment> comments = commentService.getCommentsByPost(postNo);
        return ResponseEntity.ok(comments);
    }
}
