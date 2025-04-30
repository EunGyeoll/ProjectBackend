package Project.ProjectBackend.entity;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Embeddable
@Data
public class Address {

    @NotEmpty
    private String mainAddress;

    private String detailAddress;

    @NotEmpty
    private String zipcode;

    protected Address() {}

    public Address(String mainAddress, String detailAddress, String zipcode) {
        this.mainAddress = mainAddress;
        this.detailAddress = detailAddress;
        this.zipcode = zipcode;
    }

    public String fullAddress() {
        return mainAddress + " " + detailAddress + " (" + zipcode + ")";
    }
}
