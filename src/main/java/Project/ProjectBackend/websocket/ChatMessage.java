package Project.ProjectBackend.websocket;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class ChatMessage {
    // WebSocket에서 주고받는 메시지용 엔티티
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 메시지 고유 식별 id
    private String roomId;   // 채팅방 식별 id
    private String sender;   // 보낸 사람
    private String receiver; // 받는 사람
    private String content;  // 메시지 내용
    private LocalDateTime timestamp;

    @PrePersist
    public void prePersist() {
        this.timestamp = LocalDateTime.now();
    }
}
