package Project.ProjectBackend.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;

@Embeddable
@Data
@Table(name="address")
public class Address {
    private String city;
    private String street;
    private String zipcode;

    protected Address() {
    }
    // @Embedabble인 값 타입

    public Address(String city, String street, String zipcode) {
        this.city = city;
        this.street = street;
        this.zipcode = zipcode;
    }

}
