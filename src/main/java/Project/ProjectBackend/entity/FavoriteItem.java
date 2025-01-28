package Project.ProjectBackend.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class FavoriteItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long favoriteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false) // 관심 상품 등록한 사용자
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_id", nullable = false) // Item 테이블의 item_id 와 매핑
    private Item item;

    private String representativeImagePath;


    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt; // 관심 상품 등록 시간


    public FavoriteItem(Member member, Item item){
        this.member = member;
        this.item = item;
        this.representativeImagePath = item.getRepresentativeImagePath();
    }
}
