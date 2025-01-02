package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.CommentResponseDto;
import Project.ProjectBackend.entity.Comment;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.dto.CommentRequestDto;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;
    private final MemberRepository memberRepository;

    // 댓글 작성
    @PostMapping("/comments/{postNo}")
    public ResponseEntity<String> addComment(
            @PathVariable Long postNo,
            @RequestBody CommentRequestDto requestDto
    ) {
        commentService.addComment(postNo, requestDto);
        return ResponseEntity.ok("댓글이 성공적으로 추가되었습니다.");
    }

    // 대댓글 작성
        @PostMapping("/comments/{postNo}/reply/{parentCommentId}")
        public ResponseEntity<Comment> createReply(
                @PathVariable Long postNo,
                @PathVariable Long parentCommentId,
                @RequestBody CommentRequestDto request) {
            // 작성자 조회
            Member writer = memberRepository.findById(request.getWriterId())
                    .orElseThrow(() -> new IllegalArgumentException("작성자가 존재하지 않습니다."));

            // 대댓글 생성
            Comment reply = commentService.createReply(postNo, parentCommentId, writer, request.getContent());
            return ResponseEntity.ok(reply);
        }

    // 댓글 조회
    @GetMapping("/comments/{postNo}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(@PathVariable Long postNo) {
        // 댓글 목록 조회
        List<Comment> comments = commentService.getCommentsByPost(postNo);

        // Comment -> CommentResponseDto 변환
        List<CommentResponseDto> commentResponseDtos = comments.stream()
                .map(CommentResponseDto::new) // Comment -> CommentResponseDto 변환
                .collect(Collectors.toList());

        return ResponseEntity.ok(commentResponseDtos);
    }
}
