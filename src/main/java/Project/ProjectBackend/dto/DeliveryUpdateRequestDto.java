package Project.ProjectBackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class DeliveryUpdateRequestDto {

    @NotBlank(message = "주소는 필수입니다.")
    private String mainAddress;

    @NotBlank(message = "상세주소는 필수입니다.")
    private String detailAddress;

    @NotBlank(message = "우편번호는 필수입니다.")
    private String zipcode;
}
