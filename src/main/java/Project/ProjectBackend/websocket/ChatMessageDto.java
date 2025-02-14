package Project.ProjectBackend.websocket;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter @Setter
public class ChatMessageDto {
    // WebSocket에서 주고받는 메시지용 DTO
    private String roomId;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;

    public ChatMessageDto(String sender, String receiver, String content) {
        this.roomId = generateRoomId(sender, receiver);
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // roomId 자동 생성
    public static String generateRoomId(String sender, String receiver) {
        return sender.compareTo(receiver) <0
                ? sender + "_" + receiver
                : receiver + "_" + sender ;
    }
}
