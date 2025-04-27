package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.DeliveryUpdateRequestDto;
import Project.ProjectBackend.dto.OrderCreateRequestDto;
import Project.ProjectBackend.dto.OrderDto;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Orders;
import Project.ProjectBackend.service.AuthService;
import Project.ProjectBackend.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@Slf4j
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final AuthService authService;

    // 1. 주문 생성
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/orders/new")
    public ResponseEntity<OrderDto> createOrder(@RequestBody OrderCreateRequestDto requestDto) {
        log.info("New order request received: {}", requestDto);

        Member currentUser = authService.getCurrentUser(); // 현재 로그인된 사용자

        try {
            Orders createdOrder = orderService.createOrder(requestDto, currentUser);
            log.info("주문이 성공적으로 생성되었습니다.");
            return new ResponseEntity<>(new OrderDto(createdOrder), HttpStatus.CREATED);
        } catch (Exception e) {
            log.error("주문 생성에 실패했습니다.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 2. 주문 수정 (배송지 변경)
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PutMapping("/orders/{orderId}/delivery")
    public ResponseEntity<OrderDto> updateDeliveryAddress(
            @PathVariable Long orderId,
            @RequestBody @Valid DeliveryUpdateRequestDto deliveryUpdateRequestDto) {

        Member currentUser = authService.getCurrentUser(); // 현재 로그인된 사용자

        Orders updatedOrder = orderService.updateDeliveryAddress(orderId, deliveryUpdateRequestDto, currentUser);
        return ResponseEntity.ok(new OrderDto(updatedOrder));
    }



    // 3. 주문 취소
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {

        Member currentUser = authService.getCurrentUser();

        orderService.cancelOrder(orderId, currentUser);
        return ResponseEntity.ok().build();
    }


    // 3. 주문 단건 (상세) 조회
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<OrderDto> getOrder(@PathVariable Long orderId) {

        Member currentUser = authService.getCurrentUser();

        Orders order = orderService.getOrder(orderId, currentUser);
        return ResponseEntity.ok(new OrderDto(order));
    }


    // 4. 회원별 주문 조회
    @PreAuthorize("hasAuthority('ROLE_USER') or hasAuthority('ROLE_ADMIN')")
    @GetMapping("/orders/member/{memberId}")
    public ResponseEntity<Slice<OrderDto>> getOrdersByMember(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "latest") String sortOption) {

        Member currentUser = authService.getCurrentUser();

        // 정렬 방식 설정 (최신순)
        Sort sort = sortOption.equalsIgnoreCase("latest") ? Sort.by(Sort.Direction.DESC, "orderDate") : Sort.unsorted();
        Pageable pageable = PageRequest.of(page, size, sort);

        Slice<Orders> ordersSlice = orderService.getOrdersByMember(memberId, pageable, currentUser);
        Slice<OrderDto> orderDtosSlice = ordersSlice.map(OrderDto::new);

        return ResponseEntity.ok(orderDtosSlice);
    }



}
