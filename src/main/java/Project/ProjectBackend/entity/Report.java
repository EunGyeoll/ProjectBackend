    package Project.ProjectBackend.entity;

    import jakarta.persistence.*;
    import jakarta.validation.constraints.NotBlank;
    import jakarta.validation.constraints.Size;
    import lombok.Data;
    import org.hibernate.annotations.CreationTimestamp;

    import java.time.LocalDateTime;

    @Entity
    @Data
    @Table(name="report")
    public class Report {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long reportId;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "reporter_id")
        private Member reporter; // 신고자

        @Size(max = 700, message = "신고 설명은 최대 700자까지 입력 가능합니다.")
        private String description;

        @Enumerated(EnumType.STRING)
        @Column(name = "report_type")
        private ReportType reportType;  // 신고 사유 (SPAM, HARASSMENT 등)

        @Enumerated(EnumType.STRING)
        private ReportedEntityType reportedEntityType;  // 신고 대상 유형 (ITEM, POST, ORDER, MEMBER)

        @NotBlank(message = "신고 대상 ID는 필수 입력 사항입니다.")
        private String reportedEntityId; // 신고 대상 ID

        private String reportedEntityName; // 신고 대상 ID

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "reported_member_id")
        private Member reportedMember; // 신고 대상 회원 (Member 유형일 경우)

        @CreationTimestamp
        @Column(updatable = false)
        private LocalDateTime reportDate;

        // 신고 대상에 대한 관계 설정 (예: Item, Post, Transaction 등)
        // 예시:
        // @ManyToOne
        // @JoinColumn(name = "item_id")
        // private Item item;
    }