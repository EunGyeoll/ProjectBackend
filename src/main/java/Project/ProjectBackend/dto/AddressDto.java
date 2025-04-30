package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Address;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {
    @NotBlank(message = "주소 필수 입력 항목입니다.")
    private String mainAddress;

    private String detailAddress;

    @NotBlank(message = "우편번호(zipcode)는 필수 입력 항목입니다.")
    private String zipcode;


    public AddressDto(Address address) {
        this.mainAddress = address.getMainAddress();
        this.detailAddress = address.getDetailAddress();
        this.zipcode = address.getZipcode();
    }

    public static AddressDto from(Address address) {
        return AddressDto.builder()
                .mainAddress(address.getMainAddress())
                .detailAddress(address.getDetailAddress())
                .zipcode(address.getZipcode())
                .build();
    }

}