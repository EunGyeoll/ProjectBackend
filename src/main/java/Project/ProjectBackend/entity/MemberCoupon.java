package Project.ProjectBackend.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class MemberCoupon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 관계를 식별하는 고유 ID

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member; // 회원

    @ManyToOne
    @JoinColumn(name = "coupon_id")
    private Coupon coupon; // 쿠폰

    private LocalDateTime issuedDate; // 발급일
    private LocalDateTime usedDate; // 사용일

    private Boolean isUsed = false; // 사용 여부
}
