package Project.ProjectBackend.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MemberJoinRequestDto {
    private String username;
    private String email;
    private String password;
}
