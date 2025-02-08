package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberSignupRequestDto {

    @NotBlank(message = "아이디는 필수 입력 항목입니다.")
    @Size(min = 5, max = 20, message = "아이디는 5~20자 사이여야 합니다.")
    private String memberId;

    @NotBlank(message = "비밀번호는 필수 입력 항목입니다.")
    @Size(min = 8, message = "비밀번호는 최소 8자 이상이어야 합니다.")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다.")
    private String password;

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    private String name;

    @NotBlank(message = "이메일은 필수 입력 항목입니다.")
    @Email(message = "유효한 이메일 주소를 입력해주세요.")
    private String email;

    @NotBlank(message = "전화번호는 필수 입력 항목입니다.")
    @Pattern(regexp = "^\\d{10,11}$", message = "전화번호는 숫자만 입력하며, 10~11자리여야 합니다.")
    private String phoneNum;

    @NotNull(message = "생년월일은 필수 입력 항목입니다.")
    @Past(message = "생년월일은 과거 날짜여야 합니다.")
    private LocalDate birthDate;

    @Valid
    @NotNull(message = "주소 정보는 필수 입력 항목입니다.")
    private Address address;

    @Size(max = 100, message = "상점 소개는 최대 100자까지 입력할 수 있습니다.") //
    private String shopIntroduction;

    private String profileImageUrl;
}
