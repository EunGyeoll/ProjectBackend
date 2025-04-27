package Project.ProjectBackend.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name="orders")
@Getter @Setter
public class Orders {
    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY) // 회원과 다대일 관계
    @JoinColumn(name="member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="delivery_id")
    private Delivery delivery;

    @Column
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column
    private BigDecimal discountAmount = BigDecimal.ZERO;


    public static Orders createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Orders order = new Orders();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDERED);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }


    public void cancel() {
        if (delivery.getStatus() != DeliveryStatus.ORDER_PLACED) {
            throw new IllegalStateException("주문 접수 완료 상태에서만 취소가 가능합니다.");
        }

        this.setStatus(OrderStatus.CANCELED);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    // 전체 주문 가격 조회 (할인 적용 전)
    public BigDecimal getTotalPriceBeforeDiscount() {
        return orderItems.stream()
                .map(orderItem -> BigDecimal.valueOf(orderItem.getTotalPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    // 전체 주문 가격 조회 (할인 적용 후)
    public BigDecimal getTotalPrice() {
        return getTotalPriceBeforeDiscount().subtract(discountAmount);
    }

    // 쿠폰 적용
    public void applyCoupon(Coupon coupon) {
        this.coupon = coupon;
        if (coupon.getDiscountAmount() != null) {
            this.discountAmount = BigDecimal.valueOf(coupon.getDiscountAmount());
        } else if (coupon.getDiscountRate() != null) {
            this.discountAmount = getTotalPriceBeforeDiscount().multiply(BigDecimal.valueOf(coupon.getDiscountRate()));
        }
        }

}
