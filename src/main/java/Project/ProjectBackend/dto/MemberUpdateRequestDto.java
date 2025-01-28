package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Address;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MemberUpdateRequestDto {
    // 아이디, 나이는 뺌
    private String password;
    private String name;
    private String email;
    private int age;
    private LocalDate birthDate;
    private Address address;

}
