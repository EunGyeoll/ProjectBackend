package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.CommentUpdateRequestDto;
import Project.ProjectBackend.entity.Comment;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Post;
import Project.ProjectBackend.dto.CommentRequestDto;
import Project.ProjectBackend.entity.Role;
import Project.ProjectBackend.repository.CommentRepository;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final ImageService imageService;

    // 1-1. 댓글 작성
    @Transactional
    public Comment addComment(Long postId, CommentRequestDto requestDto, Member currentUser) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        // 현재 사용자를 작성자로 설정
        Comment comment = Comment.builder()
                .post(post)
                .writer(currentUser)
                .content(requestDto.getContent())
                .build();

        return commentRepository.save(comment);
    }

    // 1-2. 대댓글 작성
    @Transactional
    public Comment addReply(Long postNo, CommentRequestDto requestDto, Member currentUser) {
        // 게시글과 부모 댓글 조회
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        Comment parentComment = commentRepository.findById(requestDto.getParentCommentId())
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));

        // 대댓글 생성
        Comment reply = Comment.builder()
                .post(post)
                .writer(currentUser)
                .content(requestDto.getContent())
                .parentComment(parentComment)
                .build();

        // 부모 댓글에 자식 댓글 추가
        parentComment.addChildComment(reply);

        // 대댓글 저장
        return commentRepository.save(reply);
    }

    @Transactional
    public Comment addCommentWithImage(Long postId, CommentRequestDto requestDto, Member currentUser, MultipartFile imageFile) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        Comment comment = Comment.builder()
                .post(post)
                .writer(currentUser)
                .content(requestDto.getContent())
                .build();

        // 이미지 저장 로직 필요
        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = imageService.saveCommentImage(imageFile, comment);
            comment.setImageUrl(imageUrl);
        }

        return commentRepository.save(comment);
    }

    @Transactional
    public Comment addReplyWithImage(Long postId, CommentRequestDto requestDto, Member currentUser, MultipartFile imageFile) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));

        Comment parent = commentRepository.findById(requestDto.getParentCommentId())
                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));

        Comment reply = Comment.builder()
                .post(post)
                .writer(currentUser)
                .content(requestDto.getContent())
                .parentComment(parent)
                .build();

        if (imageFile != null && !imageFile.isEmpty()) {
            String imageUrl = imageService.saveCommentImage(imageFile, reply);
            reply.setImageUrl(imageUrl);
        }

        parent.addChildComment(reply);
        return commentRepository.save(reply);
    }

    // 2. 댓글 수정
    @Transactional
    public void updateComment(Long commentId, CommentUpdateRequestDto dto, MultipartFile imageFile, Member currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

        // ✅ 작성자만 수정 가능
        if (!comment.getWriter().getMemberId().equals(currentUser.getMemberId())) {
            throw new AccessDeniedException("수정 권한이 없습니다.");
        }

        // ✅ 내용 수정
        comment.setContent(dto.getContent());

        // ✅ 이미지 수정 처리
        if (imageFile != null && !imageFile.isEmpty()) {
            // 기존 이미지 삭제
            if (comment.getImageUrl() != null) {
                imageService.deleteImageByPath(comment.getImageUrl());
                comment.setImageUrl(null);
            }

            // 새로운 이미지 저장
            String imageUrl = imageService.saveCommentImage(imageFile, comment);
            comment.setImageUrl(imageUrl);
        }

        // ✅ 저장
        commentRepository.save(comment);
    }




    // 3. 댓글 삭제
    @Transactional
    public void deleteComment(Long commentId, Member currentUser) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("댓글이 존재하지 않습니다."));

        // 댓글 소유자 또는 관리자 권한 확인
        if (!comment.getWriter().equals(currentUser) && !currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new IllegalArgumentException("해당 댓글을 삭제할 권한이 없습니다.");
        }

        // 댓글 삭제
        comment.markAsdeleted();
        commentRepository.save(comment);
    }


    // 4. 특정 게시글의 댓글, 대댓글 조회
    @Transactional(readOnly = true)
    public Slice<Comment> getCommentsByPost(Long postNo, Pageable pageable) {

        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

        return commentRepository.findByPostOrderByCommentDateAsc(post, pageable);
    }

}
