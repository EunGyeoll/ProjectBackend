package Project.ProjectBackend.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@NoArgsConstructor
public class DeliveryUpdateRequestDto {

    @NotBlank(message = "도시는 필수입니다.")
    private String city;

    @NotBlank(message = "도로명은 필수입니다.")
    private String street;

    @NotBlank(message = "우편번호는 필수입니다.")
    private String zipcode;
}
