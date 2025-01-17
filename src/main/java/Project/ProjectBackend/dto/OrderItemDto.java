package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.OrderItem;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderItemDto {
    private Long orderItemId;
    private Long itemId; // 상품 id
    private String itemName;
    private int orderPrice;
    private int count;
    private int totalPrice;
    private String representativeImagePath; // 대표 이미지 경로 추가


    public OrderItemDto(OrderItem orderItem) {
        this.orderItemId = orderItem.getOrderItemId();
        this.itemId = orderItem.getItem().getItemId();
        this.itemName = orderItem.getItem().getItemName();
        this.orderPrice = orderItem.getOrderPrice();
        this.count = orderItem.getCount();
        this.totalPrice = orderItem.getTotalPrice();
        this.representativeImagePath = orderItem.getItem().getRepresentativeImagePath();
    }
}