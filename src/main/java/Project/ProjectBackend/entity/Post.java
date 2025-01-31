package Project.ProjectBackend.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class Post {

    @ManyToOne(fetch = FetchType.EAGER) // 작성자와 다대일 관계
    @JoinColumn(name = "writer_id")
    @JsonIgnore
    private Member writer; //   작성자 (Member 엔티티와 연관)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long postNo; // 게시글 번호

    @Column(length = 30, nullable = false)
    private String title; // 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content; // 내용

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

    @Column(name = "REPRESENTATIVE_IMAGE_PATH")
    private String representativeImagePath; // 대표 이미지 경로

    @CreationTimestamp
    @Column(updatable = false) // 수정 시 값 변경되지 않도록 설정
    private LocalDateTime postDate;


    @OneToMany(fetch = FetchType.LAZY, mappedBy = "post", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Comment> comments = new ArrayList<>();

    @Column(nullable = false)
    private int hitCount = 0; // 조회수

    @Column(nullable = false)
    private int likeCount = 0; // 좋아요 수


    @Builder
    public Post(Member writer, String title, String content, LocalDateTime postDate) {
        this.writer = writer;
        this.title = title;
        this.content = content;
        this.postDate = postDate;
    }


    // 조회수 증가
    public void increaseHitCount() {
        this.hitCount++;
    }

    // 좋아요 증가
    public void increaseLikeCount() {
        this.likeCount++;
    }

    // 좋아요 감소
    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

}
