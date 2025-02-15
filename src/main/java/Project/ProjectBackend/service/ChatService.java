package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.ChatHistoryDto;
import Project.ProjectBackend.dto.ChatListDto;
import Project.ProjectBackend.entity.Message;
import Project.ProjectBackend.repository.ChatMessageRepository;
import Project.ProjectBackend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    // 메시지 저장
    public Message saveMessage(Message message) {
        return chatMessageRepository.save(message);
    }

    // 채팅 목록 조회 (상대방 ID와 최신 메시지 포함)
    public Slice<ChatListDto> getMemberChatList(String memberId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return chatMessageRepository.findLatestMessages(memberId, pageable)
                .map(message -> {
                    String chatPartner = message.getSender().equals(memberId)
                            ? message.getReceiver()
                            : message.getSender();

                    String profileImageUrl = memberRepository.findProfileImageUrl(chatPartner);
                    return new ChatListDto(chatPartner, message.getContent(), message.getTimestamp(), profileImageUrl);
                });
    }

    // 특정 채팅방(roomId) 내 1:1 채팅 내역 조회
    public Slice<ChatHistoryDto> getMessagesByRoomId(String roomId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId, pageable)
                .map(message -> new ChatHistoryDto(
                        message.getSender(),
                        message.getReceiver(),
                        message.getContent(),
                        message.getTimestamp()
                ));
    }


    //  특정 사용자간의 1:1 채팅 내역 조회
    public Slice<ChatHistoryDto> getMessagesBetweenUsers(String sender, String receiver, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

        return chatMessageRepository.findChatHistory(sender, receiver, pageable)
                .map(message -> new ChatHistoryDto(
                        message.getSender(),
                        message.getReceiver(),
                        message.getContent(),
                        message.getTimestamp()
                ));
    }

    // (관리자용) 모든 채팅 목록 조회
    public Slice<ChatListDto> getAllChatList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findAllChats(pageable)
                .map(message -> {
                    String chatPartner = message.getReceiver();
                    String profileImageUrl = memberRepository.findProfileImageUrl(chatPartner);
                    return new ChatListDto(chatPartner, message.getContent(), message.getTimestamp(), profileImageUrl);
                });
    }

    // (관리자용) 특정 사용자의 모든 채팅 내역 조회
    public Slice<ChatHistoryDto> getUserChatHistory(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatMessageRepository.findLatestMessages(userId, pageable)
                .map(message -> new ChatHistoryDto(
                        message.getSender(),
                        message.getReceiver(),
                        message.getContent(),
                        message.getTimestamp()
                ));
    }


}
