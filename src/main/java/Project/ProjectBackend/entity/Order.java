package Project.ProjectBackend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;

@Entity
@Table(name="orders")
@Getter @Setter
public class Order {
    /**
     * - 회원(Member)와 다대일 관계.
     * - 주문상품(OrderItem)과 일대다 관계.
     * - 배송(Delivery)와 일대일 관계.
     */

    @Id
    @GeneratedValue
    @Column(name = "order_id")
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY) // 회원과 다대일 관계
    @JoinColumn(name="member_id")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    // 이렇게 초기화해야 함. @OneToMany 관계에서 List는 기본적으로 null이다. 즉, Hibernate나 JPA가 초기화해 주지 않으므로, 직접 초기화해줘야 합니다.
    // 초기화하면 null이 아닌 빈 리스트가 설정된다. @OneToMany 같은 컬렉션 필드는 항상 초기화하는 것이 좋다. 이러면 NullPointException 방지 가능.

    /**
     * 주문상품(OrderItem)과 일대다 관계.
     * - mappedBy="order": 연관 관계의 주인은 OrderItem의 "order" 필드.
     * - cascade = CascadeType.ALL: Order 저장/삭제 시 OrderItem도 함께 처리.
     * - orphanRemoval = true: Order에서 제거된 OrderItem은 자동으로 삭제.
     */

    @OneToOne(fetch = LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name="delivery_id")
    private Delivery delivery;

    private LocalDateTime OrderDate;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    //==생성 메서드==//
    public static Order createOrder(Member member, Delivery delivery, OrderItem... orderItems) {
        Order order = new Order();
        order.setMember(member);
        order.setDelivery(delivery);
        for (OrderItem orderItem : orderItems) {
            order.addOrderItem(orderItem);
        }
        order.setStatus(OrderStatus.ORDER);
        order.setOrderDate(LocalDateTime.now());
        return order;
    }

    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }


    public void cancel() {
        if (delivery.getStatus() == DeliveryStatus.COMP) {
            throw new IllegalStateException("이미 배송완료된 상품은 취소가 불가능합니다.");
        }

        this.setStatus(OrderStatus.CANCEL);
        for (OrderItem orderItem : orderItems) {
            orderItem.cancel();
        }
    }

    /**
     * 전체 주문 가격 조회
     */
    public int getTotalPrice() {
        int totalPrice = 0;
        for (OrderItem orderItem : orderItems) {
            totalPrice += orderItem.getTotalPrice();
        }
        return totalPrice;
    }

}
