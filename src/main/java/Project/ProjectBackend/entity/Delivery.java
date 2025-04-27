package Project.ProjectBackend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
@Table(name="delivery")
public class Delivery {
    @Id @GeneratedValue
    @Column(name = "delivery_id")
    private Long deliveryId;

    @JsonIgnore
    @OneToOne(mappedBy = "delivery", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Orders order;

    @Embedded
    private Address address;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_status")
    private DeliveryStatus status;

    // 배송 상태 변경
    public void changeStatus(DeliveryStatus newStatus) {
        this.status = newStatus;
    }

    // 배송지 변경
    public void updateAddress(Address newAddress) {
        this.address = newAddress;
    }

}
