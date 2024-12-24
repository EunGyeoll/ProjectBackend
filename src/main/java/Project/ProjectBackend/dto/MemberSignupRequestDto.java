package Project.ProjectBackend.dto;

import Project.ProjectBackend.domain.Address;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberSignupRequestDto {
    private String id;
    private String password;
    private String name;
    private String email;
    private int age;
    private Address address; // 추가: Address를 포함하도록 수정



}
