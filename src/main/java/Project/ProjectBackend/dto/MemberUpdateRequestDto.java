package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Address;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class MemberUpdateRequestDto {
    // 아이디, 나이는 뺌
    private String name;
    private String currentPassword;  // 기존 비밀번호 확인용
    private String newPassword;  // 새 비밀번호 (선택)
    private String email;
    private int age;
    private LocalDate birthDate;
    private Address address;
    @Size(max = 100, message = "상점 소개는 최대 100자까지 입력할 수 있습니다.")
    private String shopIntroduction;
    private String profileImageUrl;
}
