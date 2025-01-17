package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Address;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AddressDto {
    private String street;
    private String city;
    private String zipcode;


    public AddressDto(Address address) {
        this.street = address.getStreet();
        this.city = address.getCity();
        this.zipcode = address.getZipcode();
    }


}