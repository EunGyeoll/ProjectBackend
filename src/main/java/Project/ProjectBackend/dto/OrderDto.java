package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Orders;
import Project.ProjectBackend.entity.OrderStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
public class OrderDto {
    private Long orderId;
    private String memberId;
    private String memberName;
    private List<OrderItemDto> orderItems;
    private DeliveryDto delivery;
    private LocalDateTime orderDate;
    private OrderStatus status;
    private BigDecimal totalPrice;
    private String couponCode; // 추가
    private BigDecimal discountAmount; // 추가

    public OrderDto(Orders order) {
        this.orderId = order.getOrderId();
        this.memberId = order.getMember().getMemberId();
        this.memberName = order.getMember().getMemberName();
        this.orderItems = order.getOrderItems().stream()
                .map(OrderItemDto::new)
                .collect(Collectors.toList());
        this.delivery = new DeliveryDto(order.getDelivery());
        this.orderDate = order.getOrderDate();
        this.status = order.getStatus();
        this.totalPrice = order.getTotalPrice();

        if (order.getCoupon() != null) {
            this.couponCode = order.getCoupon().getCouponCode();
            this.discountAmount = order.getDiscountAmount();
        }
    }
}
