package Project.ProjectBackend.websocket;

import Project.ProjectBackend.dto.ChatHistoryDto;
import Project.ProjectBackend.dto.ChatListDto;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.entity.Message;
import Project.ProjectBackend.service.AuthService;
import Project.ProjectBackend.service.ChatService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.prepost.PreAuthorize;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Slice;

@RestController
@RequiredArgsConstructor
@Slf4j
public class ChatController {
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    private final AuthService authService;

    // WebSocket을 통한 1:1 메시지 전송
    @MessageMapping("/chat/message")
    public void privateMessage(ChatMessageDto messageDto,
                               @Headers Map<String, Object> headers) {
        // 🔥 WebSocket 세션에서 `memberId` 가져오기
        Map<String, Object> sessionAttributes = (Map<String, Object>) headers.get("simpSessionAttributes");
        String memberId = (sessionAttributes != null) ? (String) sessionAttributes.get("memberId") : null;

        if (memberId == null) {
            log.error("WebSocket 세션에 사용자 ID 없음");
            throw new SecurityException("인증되지 않은 사용자입니다.");
        }

        log.info("📩 수신된 메시지 - 보낸 사람: {}", memberId);

        // 메시지 저장 및 전송
        Message message = Message.builder()
                .roomId(ChatMessageDto.generateRoomId(memberId, messageDto.getReceiver()))
                .sender(memberId)
                .receiver(messageDto.getReceiver())
                .content(messageDto.getContent())
                .timestamp(LocalDateTime.now())
                .build();


        // 🔹 채팅 상대방에게 실시간 메시지 전송
        messagingTemplate.convertAndSend("/sub/chat/private/" + messageDto.getReceiver(), messageDto);

        chatService.saveMessage(message);
    }



    // 특정 사용자의 채팅 목록 조회
    @GetMapping("/chat/list/{memberId}")
    public Slice<ChatListDto> getChatList(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        // 현재 로그인한 사용자 확인
        Member currentUser = authService.getCurrentUser();

        // 본인 또는 관리자만 접근 가능
        if (!currentUser.getMemberId().equals(memberId) && !currentUser.getRole().equals("ROLE_ADMIN")) {
            throw new SecurityException("본인만 채팅 목록을 조회할 수 있습니다.");
        }

        log.info("{} 사용자가 자신의 채팅 목록 조회", currentUser.getMemberId());
        return chatService.getMemberChatList(memberId, page, size);
    }


    // 특정 사용자간의 채팅 내역 조회
    @GetMapping("/chat/history/{sender}/{receiver}")
    public Slice<ChatHistoryDto> getChatHistory(
            @PathVariable String sender,
            @PathVariable String receiver,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        // 현재 로그인한 사용자 확인
        Member currentUser = authService.getCurrentUser();

        // 본인 또는 관리자만 접근 가능
        if (!isAuthorizedToViewChat(currentUser, sender, receiver)) {
            throw new SecurityException("본인의 채팅 내역만 조회할 수 있습니다.");
        }

        log.info("{} 사용자가 {} & {} 간의 채팅 내역 조회", currentUser.getMemberId(), sender, receiver);
        return chatService.getMessagesBetweenUsers(sender, receiver, page, size);
    }


    // 권한 체크 메서드
    private boolean isAuthorizedToViewChat(Member currentUser, String sender, String receiver) {
        return currentUser.getMemberId().equals(sender) ||
                currentUser.getMemberId().equals(receiver) ||
                currentUser.getRole().equals("ROLE_ADMIN");

    }

}