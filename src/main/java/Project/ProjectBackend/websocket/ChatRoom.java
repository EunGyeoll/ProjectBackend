package Project.ProjectBackend.websocket;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter
@Setter
public class ChatRoom {
    // 향후에 그룹 채팅 기능을 추가할 가능성이 있으므로 해당 엔티티 생성해 둠
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // DB에서 자동 증가되는 ID

    private String roomId;  // UUID로 채팅방 고유 ID 생성
    private String name;  // 채팅방 이름

    public static ChatRoom create(String name) {
        ChatRoom chatRoom = new ChatRoom();
        chatRoom.roomId = UUID.randomUUID().toString();
        chatRoom.name = name;
        return chatRoom;
    }
}
