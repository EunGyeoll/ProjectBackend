package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class MemberSimpleDto {
    private String memberId;
    private String name;
    private String email;
    private Role role;
    private boolean enabled;
    private LocalDate birthDate;
    private String phoneNum;
    private AddressDto address;
    private LocalDateTime registrationDate;

    // 엔티티를 DTO로 변환하는 정적 메서드
    public static MemberSimpleDto from(Member member) {
        return MemberSimpleDto.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .enabled(member.isEnabled())
                .birthDate(member.getBirthDate())
                .phoneNum(member.getPhoneNum())
                .registrationDate(member.getRegistrationDate())
                .address(AddressDto.from(member.getAddress()))
                .build();
    }
}
