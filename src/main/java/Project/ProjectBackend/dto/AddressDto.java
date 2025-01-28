package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Address;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddressDto {
    private String street;
    private String city;
    private String zipcode;


    public AddressDto(Address address) {
        this.street = address.getStreet();
        this.city = address.getCity();
        this.zipcode = address.getZipcode();
    }

    public static AddressDto from(Address address) {
        return AddressDto.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .zipcode(address.getZipcode())
                .build();
    }

}