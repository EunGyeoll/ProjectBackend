package Project.ProjectBackend.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
@NoArgsConstructor
public class Member {
    @Id
    @Column(name = "member_id")
    private String id;

    @NotEmpty
    private String password;

    @NotEmpty
    private String name;

    @NotEmpty
    private String email;

    @NotNull
    private int age;
    // 숫자 필드엔 @NotEmpty 대신 @NotNull 또는 @Positive, @Min, @Max 등의 애노테이션을 사용

    @Embedded
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    @Builder
    public Member(String id, String name, String email, String password, Address address, int age) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.email = email;
        this.address = address;
        this.age = age; // age 추가
    }
}
