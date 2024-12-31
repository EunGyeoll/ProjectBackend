package Project.ProjectBackend.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId; // 댓글 식별ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false)
    private Member writerId; // 댓글 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post; // 댓글이 속한 게시글

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 댓글 내용

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime commentDate; // 댓글 작성 시간

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment; // 부모 댓글 (대댓글 구현하기 위해 필요)

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> childComments = new ArrayList<>(); // 대댓글 리스트

    @Builder
    public Comment(Post post, Member writerId, String content, Comment parentComment) {
        this.post = post;
        this.writerId = writerId;
        this.content = content;
        this.parentComment = parentComment;
    }

    public void addChildComment(Comment childComment) {
        this.childComments.add(childComment);  // 부모의 자식 리스트에 추가
        childComment.setParentComment(this);  // 자식 댓글의 부모 설정
    }

    private void setParentComment(Comment parentComment) {
        this.parentComment = parentComment;  // 부모 댓글 설정
    }

}
