package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Delivery;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class DeliveryDto {
    private AddressDto address;
    private String status;

    public DeliveryDto(Delivery delivery) {
        this.address = new AddressDto(delivery.getAddress()); // AdressDto  객체로 설정
        this.status = delivery.getStatus().name(); // Enum을 문자열로 변환
    }
}