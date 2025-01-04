package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Address;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MemberSignupRequestDto {
    private String memberId;
    private String password;
    private String name;
    private String email;
    private String role;
    private String phoneNum;
    private LocalDate birthDate;
    private Address address; // 추가: Address를 포함하도록 수정

}
