package Project.ProjectBackend.service;

import Project.ProjectBackend.entity.Comment;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Post;
import Project.ProjectBackend.dto.CommentRequestDto;
import Project.ProjectBackend.repository.CommentRepository;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    // 댓글 작성
    @Transactional
    public void addComment(Long postId, CommentRequestDto requestDto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("해당 게시글이 존재하지 않습니다."));
        Member writer = memberRepository.findById(requestDto.getWriterId())
                .orElseThrow(() -> new IllegalArgumentException("작성자가 존재하지 않습니다."));

        Comment comment = Comment.builder()
                .post(post)
                .writer(writer) // 영속 상태의 Member 객체 사용
                .content(requestDto.getContent())
                .build();

        commentRepository.save(comment);
    }


        // 대댓글 작성
        @Transactional
        public Comment createReply(Long postNo, Long parentCommentId, Member writer, String content) {
            // 게시글과 부모 댓글 조회
            Post post = postRepository.findById(postNo)
                    .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));

            Comment parentComment = commentRepository.findById(parentCommentId)
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));

            // 대댓글 생성
            Comment reply = Comment.builder()
                    .post(post)
                    .writer(writer)  // 작성자 설정
                    .content(content)
                    .parentComment(parentComment)
                    .build();

            // 부모 댓글에 자식 댓글 추가
            parentComment.addChildComment(reply);

            // 대댓글 저장
            return commentRepository.save(reply);
        }
//    public Comment createReply(Long postNo, Long parentCommentId, Member writer, String content) {
//        Post post = postRepository.findById(postNo)
//                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
//
//        Comment parentComment = commentRepository.findById(parentCommentId)
//                .orElseThrow(() -> new IllegalArgumentException("부모 댓글이 존재하지 않습니다."));
//
//        Comment reply = Comment.builder()
//                .post(post)
//                .writer(writer)
//                .content(content)
//                .parentComment(parentComment)
//                .build();
//
//        parentComment.addChildComment(reply);
//        return commentRepository.save(reply);
//    }

    // 특정 게시글의 댓글 및 대댓글 조회
    @Transactional(readOnly = true)
    public List<Comment> getCommentsByPost(Long postNo) {
        Post post = postRepository.findById(postNo)
                .orElseThrow(() -> new IllegalArgumentException("게시글이 존재하지 않습니다."));
        return commentRepository.findByPost(post);
    }
}
