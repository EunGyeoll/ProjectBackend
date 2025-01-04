package Project.ProjectBackend.entity;

import Project.ProjectBackend.exception.NotEnoughStockException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
public class Item {

    @ManyToOne(fetch = FetchType.LAZY) // 다대일 관계 설정
    @JoinColumn(name = "member_id") // 외래 키 컬럼 명시
    private Member seller; // 판매자

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ID 자동 생성
    @Column(name = "item_id")
    private Long itemId;

//    @NotEmpty
//    private String imagePath; // 이미지 파일 경로 또는 URL

    @NotEmpty(message = "상품명은 필수 항목입니다.")
    @Column(name = "ITEM_NAME") // 컬럼 이름 매핑
    private String itemName; // 상품명

    @NotNull(message = "가격은 필수 항목입니다.")
    @Positive(message = "가격은 양수여야 합니다.")
    private Integer price; // 가격

    @NotEmpty(message = "상품 설명은 필수 항목입니다.")
    @Column(length = 1000)
    private String description; // 상품 설명

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>(); // 여러 이미지를 관리

    @Column(name = "REPRESENTATIVE_IMAGE_PATH")
    private String representativeImagePath; // 대표 이미지 경로

    @NotNull(message = "수량은 필수 항목입니다.")
    private Integer stockQuantity; // 재고 수량

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @CreationTimestamp
    @Column(name = "ITEM_DATE", updatable = false) // 수정 시 날짜 변경되지 않도록
    private LocalDateTime itemDate;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FavoriteItem> favoriteItems = new ArrayList<>();
    // orphanRemoval = true: Item과 관계가 끊어진 Favorites도 삭제됨

    @Column(name = "HIT_COUNT", nullable = false)
    private int hitCount = 0;

    @Column(name = "FAVORITE_COUNT", nullable = false)
    private int favoriteCount = 0;

    // 조회수 증가 메소드
    public void increaseHitCount() {
        this.hitCount++;
    }

    // 찜 수 증가 메소드
    public void increaseFavoriteCount() {
        this.favoriteCount++;
    }

    // 찜 수 감소 메소드
    public void decreaseFavoriteCount() {
        if (this.favoriteCount > 0) {
            this.favoriteCount--;
        }
    }


    // 재고 증가 메소드
    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }

    // 재고 감소 매소드
    public void removeStock(int quantity) {
        int restStock = this.stockQuantity - quantity;
        if (restStock < 0) {
            throw new NotEnoughStockException("need more stock");
        }
        this.stockQuantity = restStock;
    }


    public Item(Member seller, String itemName, Integer price, String description, Integer stockQuantity, Category category, LocalDateTime itemDate) {
        this.seller = seller;
        this.itemName = itemName;
        this.price = price;
        this.description = description;
        this.stockQuantity = stockQuantity;
        this.category = category;
        this.itemDate = itemDate;
    }
}
