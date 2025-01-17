package Project.ProjectBackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class OrderCreateRequestDto {

    @NotNull(message = "회원 아이디는 필수입니다.")
    private String memberId;

    @NotNull(message = "상품 ID는 필수입니다.")
    private Long itemId;

    @Min(value = 1, message = "상품 수량은 최소 1개 이상이어야 합니다.")
    private Integer count; // Integer로 변경해서 null 허용

    // 배송 주소 정보
    @NotNull(message = "배송 주소는 필수입니다.")
    private AddressDto address;

    private String couponCode;


}