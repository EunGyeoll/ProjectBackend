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
    private String memberId;
    @NotEmpty
    private String password;
    @NotEmpty
    private String name;
    @NotEmpty
    private String email;

    @NotNull
    @Column(name = "birth_date")
    private String birthDate;
    @NotEmpty
    private String role;
    @NotEmpty
    @Column(name = "phone_num")
    private String phoneNum;
    // 숫자 필드엔 @NotEmpty 대신 @NotNull 또는 @Positive, @Min, @Max 등의 애노테이션을 사용
    @Embedded
    private Address address;

    @JsonIgnore
    @OneToMany(mappedBy = "member")
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "writer", cascade = CascadeType.ALL, orphanRemoval = true) // 양방향 관계 설정
    private List<Post> posts = new ArrayList<>(); // 작성한 게시글 목록


    @Builder
    public Member(String memberId, String password, String name, String email, String birthDate, String role,  Address address, String phoneNum) {
        this.memberId = memberId;
        this.password = password;
        this.name = name;
        this.email = email;
        this.birthDate = birthDate; // age 추가
        this.role = role;
        this.phoneNum=phoneNum;
        this.address = address;
    }
}
