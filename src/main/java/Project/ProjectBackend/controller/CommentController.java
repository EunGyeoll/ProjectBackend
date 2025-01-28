package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.CommentResponseDto;
import Project.ProjectBackend.dto.CommentUpdateRequestDto;
import Project.ProjectBackend.entity.Comment;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.dto.CommentRequestDto;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.service.AuthService;
import Project.ProjectBackend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROLE_USER')")
public class CommentController {

    private final CommentService commentService;
    private final AuthService authService;


    // 1. 댓글 및 대댓글 작성
    @PostMapping("/comments/{postNo}")
    public ResponseEntity<CommentResponseDto> createComment(
                @PathVariable Long postNo, @RequestBody @Valid  CommentRequestDto commentRequestDto) {
        Member currentUser = authService.getCurrentUser();

        Comment comment;

        if(commentRequestDto.getParentCommentId() == null) {
            // 댓글 작성
            comment = commentService.addComment(postNo, commentRequestDto, currentUser);
        } else {
            // 대댓글 작성
            comment = commentService.addReply(postNo, commentRequestDto, currentUser);
        }
        return ResponseEntity.ok(new CommentResponseDto(comment));
    }



    // 2. 댓글 수정
    @PutMapping("/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestBody @Valid CommentUpdateRequestDto updateRequestDto) {

        Member currentUser = authService.getCurrentUser();

        Comment updatedComment = commentService.updateComment(commentId, updateRequestDto, currentUser);

        return ResponseEntity.ok(new CommentResponseDto(updatedComment));
    }



    // 3. 댓글 삭제
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<String> deleteComment(
            @PathVariable Long commentId) {

        Member currentUser = authService.getCurrentUser();

        commentService.deleteComment(commentId, currentUser);
        return ResponseEntity.ok("댓글이 성공적으로 삭제되었습니다.");
    }


    // 4. 게시글의 댓글, 대댓글 조회
    @GetMapping("/comments/{postNo}")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByPost(
            @PathVariable Long postNo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
            ) {

        Pageable pageable = PageRequest.of(page,size);

        // 댓글 목록 조회
        Slice<Comment> comments = commentService.getCommentsByPost(postNo, pageable);


        // Comment -> CommentResponseDto 변환
        List<CommentResponseDto> commentResponseDtos = comments.getContent()
                .stream()
                .map(CommentResponseDto::new)
                .collect(Collectors.toList());

        return ResponseEntity.ok(commentResponseDtos);
    }

}
