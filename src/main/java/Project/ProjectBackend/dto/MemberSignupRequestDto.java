package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Address;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberSignupRequestDto {
    private String memberId;
    private String password;
    private String name;
    private String email;
    private String birthDate;
    private String role;
    private String phoneNum;
    private Address address; // 추가: Address를 포함하도록 수정

}
