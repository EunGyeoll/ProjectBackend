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
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        Slice<Message> chatList = chatMessageRepository.findLatestMessages(memberId, pageable);

        List<ChatListDto> dtoList = new ArrayList<>();

        Set<String> uniqueChatPartners = new HashSet<>();

        for (Message message : chatList) {

            String chatPartner =
                    message.getSender().equals(memberId)
                            ? message.getReceiver()
                            : message.getSender();

            // 중복 방지
            if (!uniqueChatPartners.add(chatPartner)) continue;

            // 상대방 닉네임 조회
            String nickname = memberRepository.findNicknameByMemberId(chatPartner);

            // 상대방 프로필 이미지
            String profileImageUrl = memberRepository.findProfileImageUrl(chatPartner);

            // 미읽은 메시지 수 구하기 (나중에 구현)
            long unreadCount = 0L;

            // DTO 생성
            ChatListDto dto = new ChatListDto(
                    chatPartner,
                    nickname,
                    message.getContent(),
                    message.getTimestamp(),
                    profileImageUrl,
                    unreadCount
            );

            dtoList.add(dto);
        }

        return new SliceImpl<>(dtoList, pageable, chatList.hasNext());
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


    //  두 사람간의 1:1 채팅 내역 조회
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

    // 관리자: 모든 채팅 목록 조회
    public Slice<ChatListDto> getAllChatList(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return chatMessageRepository.findAllChats(pageable)
                .map(message -> {

                    String chatPartner = message.getReceiver();
                    String profileImageUrl = memberRepository.findProfileImageUrl(chatPartner);

                    return new ChatListDto(
                            chatPartner,
                            null,                       // 관리자에서는 닉네임 불필요
                            message.getContent(),
                            message.getTimestamp(),
                            profileImageUrl,
                            0L                          // 읽음처리는 나중에
                    );
                });
    }

    //  관리자: 특정 유저의 메시지 전체 조회
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
