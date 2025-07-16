package Project.ProjectBackend.entity;

import Project.ProjectBackend.dto.ReviewResponseDto;
import Project.ProjectBackend.entity.Item;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name="image")
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long imageId;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime imageDate;

    @Column(nullable = false)
    private String originFileName;

    @Column
    private String newFileName;

    @Column
    private String imagePath; // 이미지 경로

    @Column
    private long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id")
    private Item item; // 해당 이미지를 참조하는 Item

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post; // 해당 이미지를 참조하는 Post

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id")
    private Review review; // 해당 이미지를 참조하는 Review

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comment_id")
    @JsonIgnore
    private Comment comment;


    public void setPost(Post post) {
        this.post = post;
        if (post != null && !post.getImages().contains(this)) {
            post.getImages().add(this);
        }
    }

}
