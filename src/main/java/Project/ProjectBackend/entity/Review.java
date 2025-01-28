package Project.ProjectBackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "writer_id", nullable = false) // 작성자
    private Member writer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "store_owner_id", nullable = false) // 상점 주인
    private Member storeOwner;

    @Column(nullable = false, length = 1000)
    private String content; // 리뷰 내용

    @Column(nullable = false)
    private int rating; // 평점 (1~5)

    @OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Image> images = new ArrayList<>(); // 여러 이미지를 관리

    @Column(name = "REPRESENTATIVE_IMAGE_PATH")
    private String representativeImagePath;


    @CreationTimestamp
    private LocalDateTime createdAt; // 리뷰 작성 시간

    // Helper method to add image
    public void addImage(Image image) {
        images.add(image);
        image.setReview(this);
    }

    // Helper method to remove image
    public void removeImage(Image image) {
        images.remove(image);
        image.setReview(null);
    }
}
