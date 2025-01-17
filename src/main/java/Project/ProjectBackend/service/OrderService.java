package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.DeliveryUpdateRequestDto;
import Project.ProjectBackend.dto.OrderCreateRequestDto;
import Project.ProjectBackend.entity.*;
import Project.ProjectBackend.repository.CouponRepository;
import Project.ProjectBackend.repository.ItemRepository;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final CouponRepository couponRepository;

    // 1. 주문 생성
    @Transactional
    public Order createOrder(OrderCreateRequestDto requestDto) {
        // 회원 조회
        Member member = memberRepository.findById(requestDto.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        // 상품 조회
        Item item = itemRepository.findById(requestDto.getItemId())
                .orElseThrow(() -> new IllegalArgumentException("해당 상품이 존재하지 않습니다."));

        // 수량 설정
        int count = (requestDto.getCount() != null) ? requestDto.getCount() : 1; // 기본 수량은 1로 지정
        if (item.getStockQuantity() < count) {
            throw new IllegalArgumentException("요청한 수량이 상품 재고를 초과합니다.");
        }

        // 배송 주소 정보 변환
        Address address = new Address(
                requestDto.getAddress().getCity(),
                requestDto.getAddress().getStreet(),
                requestDto.getAddress().getZipcode()
        );

        Delivery delivery = new Delivery();
        delivery.setAddress(address);
        delivery.setStatus(DeliveryStatus.ORDER_PLACED);

        // 주문 아이템 생성
        OrderItem orderItem = OrderItem.createOrderItem(item, item.getPrice(), count);

        // 주문 생성
        Order order = Order.createOrder(member, delivery, orderItem);

        // 쿠폰 적용 (선택사항)
        Coupon coupon = null;
        if (requestDto.getCouponCode() != null && !requestDto.getCouponCode().isEmpty()) {
            coupon = couponRepository.findByCouponCode(requestDto.getCouponCode())
                    .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 쿠폰 코드입니다."));
            // 쿠폰의 유효성 검사 (기간, 사용 가능 여부 등 추가 가능)
        }

        // 쿠폰 적용
        if (coupon != null) {
            order.applyCoupon(coupon);
        }

        // 주문 저장
        orderRepository.save(order);

        return order;
    }


    // 2. 주문 수정 (배송지 변경)
    @Transactional
    public Order updateDeliveryAddress(Long orderId, DeliveryUpdateRequestDto deliveryUpdateRequestDto) {
        // 현재 인증된 사용자 정보 가져오기
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName(); // 회원 ID 가져옴

        // 주문 조회
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));

        // 주문 소유자 확인
        if (!order.getMember().getMemberId().equals(currentUsername)) {
            throw new IllegalArgumentException("해당 주문을 수정할 권한이 없습니다.");
        }

        // 주문 상태 확인
        Delivery delivery = order.getDelivery();
        if ( delivery.getStatus() != DeliveryStatus.ORDER_PLACED && delivery.getStatus() != DeliveryStatus.ORDER_CONFIRMED ) {
            throw new IllegalArgumentException("배송지 변경은 주문 접수 완료, 주문 확인 완료 상태에서만 가능합니다.");
        }

        // 새로운 주소 생성
        Address newAddress = new Address(
                deliveryUpdateRequestDto.getCity(),
                deliveryUpdateRequestDto.getStreet(),
                deliveryUpdateRequestDto.getZipcode()
        );

        // 배송지 업데이트
        delivery.updateAddress(newAddress);

        // 변경된 주문 저장
        return orderRepository.save(order);
    }

     // 주문 조회
    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));
    }

    // 특정 회원의 모든 주문 조회
    @Transactional(readOnly = true)
    public Slice<Order> getOrdersByMember(String memberId, Pageable pageable) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다."));

        return orderRepository.findByMember(member, pageable);
    }


    // 주문 취소
    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문이 존재하지 않습니다."));
        order.cancel();
    }

}
