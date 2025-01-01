package Project.ProjectBackend.entity;

import Project.ProjectBackend.exception.NotEnoughStockException;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

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

    @NotEmpty
    private String itemName; // 상품명

    @NotNull
    private Integer price; // 가격

    @NotEmpty
    @Column(length = 1000)
    private String description; // 상품 설명

    @NotNull
    private Integer stockQuantity; // 재고 수량

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @CreationTimestamp
    @Column(updatable = false) // 수정 시 값 변경되지 않도록 설정
    private LocalDateTime itemDate;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Favorite> favorites = new ArrayList<>();
    // orphanRemoval = true: Item과 관계가 끊어진 Favorites도 삭제됨

    public void addStock(int quantity) {
        this.stockQuantity += quantity;
    }


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
