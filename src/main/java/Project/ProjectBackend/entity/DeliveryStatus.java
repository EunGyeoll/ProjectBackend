package Project.ProjectBackend.entity;


public enum DeliveryStatus {
    ORDER_PLACED,       // 주문 접수
    ORDER_CONFIRMED,    // 주문 확정
    SHIPPED,            // 배송 시작
    DELIVERED,          // 배송 완료
    CANCELED            // 주문 취소
}
