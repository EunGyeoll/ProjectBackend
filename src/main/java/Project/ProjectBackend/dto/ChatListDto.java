package Project.ProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
@AllArgsConstructor
public class ChatListDto {
    private String chatPartner;   // 상대방 ID
    private String lastMessage;   // 가장 최근 메시지
    private LocalDateTime timestamp; // 가장 최근 메시지 시간
    private String partnerProfileImgUrl; // 상대방 프로필 이미지
}
