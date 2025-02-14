package Project.ProjectBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ChatHistoryDto {
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;

    public ChatHistoryDto(String sender, String receiver, String content, LocalDateTime timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = timestamp;
    }
}
