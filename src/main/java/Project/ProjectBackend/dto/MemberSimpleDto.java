package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
@Slf4j
public class MemberSimpleDto {
    private String memberId;
    private String name;
    private String nickName;
    private String email;
    private Role role;
    private boolean enabled;
    private LocalDate birthDate;
    private String phoneNum;
    private AddressDto address;
    private LocalDateTime registrationDate;
    private String profileImageUrl;



    // 엔티티를 DTO로 변환하는 정적 메서드
    public static MemberSimpleDto from(Member member) {
        String profileImageUrl = null;

        if (member.getProfileImage() != null) {
            profileImageUrl = member.getProfileImage().getImagePath(); // S3 URL 직접 사용
            log.info("최종 프로필 이미지 URL: {}", profileImageUrl);
        }


        return MemberSimpleDto.builder()
                .memberId(member.getMemberId())
                .name(member.getMemberName())
                .nickName(member.getNickName())
                .email(member.getEmail())
                .role(member.getRole())
                .enabled(member.isEnabled())
                .birthDate(member.getBirthDate())
                .phoneNum(member.getPhoneNum())
                .registrationDate(member.getRegistrationDate())
                .address(AddressDto.from(member.getAddress()))
                .profileImageUrl(profileImageUrl)
                .build();
    }
}
