package Project.ProjectBackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class ChatListDtoForAdmin {
    private String sender;
    private String receiver;
    private String lastMessage;
    private LocalDateTime timestamp; // 가장 최근 메시지 시간
}