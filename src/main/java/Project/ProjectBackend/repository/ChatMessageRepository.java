package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Message;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatMessageRepository extends JpaRepository<Message, Long> {

    // 채팅 목록
    @Query("SELECT m FROM Message m " +
            "WHERE m.id IN ( " +
            "  SELECT MAX(m2.id) FROM Message m2 " +
            "  WHERE m2.sender = :memberId OR m2.receiver = :memberId " +
            "  GROUP BY m2.roomId " +
            ") ORDER BY m.timestamp DESC")
    Slice<Message> findLatestMessages(String memberId, Pageable pageable);

    // 특정 채팅방(roomId) 내 1:1 채팅 내역 조회
    Slice<Message> findByRoomIdOrderByTimestampAsc(String roomId, Pageable pageable);

    // 두 명간의 채팅 내역 조회
    @Query("SELECT m FROM Message m " +
            "WHERE (m.sender = :sender AND m.receiver = :receiver) " +
            "   OR (m.sender = :receiver AND m.receiver = :sender) " +
            "ORDER BY m.timestamp DESC")
    Slice<Message> findChatHistory(@Param("sender") String sender,
                                   @Param("receiver") String receiver,
                                   Pageable pageable);


    // 전체 채팅 목록 조회 (관리자용)
    @Query("SELECT m FROM Message m " +
            "WHERE m.timestamp = (SELECT MAX(m2.timestamp) FROM Message m2 " +
            "WHERE m2.roomId = m.roomId) " +
            "ORDER BY m.timestamp DESC")
    Slice<Message> findAllChats(Pageable pageable);
}
