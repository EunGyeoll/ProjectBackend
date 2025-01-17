package Project.ProjectBackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
public class Coupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long couponId; // 쿠폰의 고유 ID

    private String couponCode; // 쿠폰 코드 (예: "DISCOUNT10")

    private Integer discountAmount; // 정액 할인 (예: 5000원)

    private Double discountRate; // 정률 할인 (예: 0.1 = 10%)

    private Integer minPurchaseAmount; // 최소 구매 금액 조건 (예: 50000원)

    private LocalDateTime startDate; // 사용 시작일
    private LocalDateTime endDate; // 사용 종료일

    @OneToMany(mappedBy = "coupon", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MemberCoupon> memberCoupons = new ArrayList<>(); // 중간 엔티티

    }

