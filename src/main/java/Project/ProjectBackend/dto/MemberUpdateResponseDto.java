package Project.ProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MemberUpdateResponseDto {
    private String memberId;
    private String memberName;
    private String nickName;
    private String email;
    private String phoneNum;
    private AddressDto address;
    private String profileImageUrl;
}
