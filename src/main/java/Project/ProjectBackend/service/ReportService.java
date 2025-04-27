package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.ReportCreateDto;
import Project.ProjectBackend.dto.ReportResponseDto;
import Project.ProjectBackend.entity.*;
import Project.ProjectBackend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;
    private final MemberRepository memberRepository;
    private final ItemRepository itemRepository;
    private final PostRepository postRepository;
    private final AuthService authService; // 현재 사용자 정보를 가져오는 서비스
    private final OrderRepository orderRepository;


    //신고 생성
    @Transactional
    public ReportResponseDto createReport(ReportCreateDto reportCreateDto, Member currentUser) {
        // 신고자 조회
        Member reporter = memberRepository.findByMemberId(currentUser.getMemberId())
                .orElseThrow(() -> new IllegalArgumentException("신고자가 존재하지 않습니다."));

        // 신고 대상 유효성 검증
        validateReportedEntity(reportCreateDto.getReportedEntityType(), reportCreateDto.getReportedEntityId());

        // 신고 대상에 따른 reportedMember 설정
        Member reportedMember = null;
        if (reportCreateDto.getReportedEntityType() == ReportedEntityType.MEMBER) {
            reportedMember = memberRepository.findByMemberId(reportCreateDto.getReportedEntityId())
                    .orElseThrow(() -> new IllegalArgumentException("신고 대상 사용자가 존재하지 않습니다."));
        }

        // Report 엔티티 생성
        Report report = new Report();
        report.setReporter(reporter);
        report.setReportedEntityType(reportCreateDto.getReportedEntityType());
        report.setReportedEntityId(reportCreateDto.getReportedEntityId());
        report.setReportedEntityName(getReportedEntityName(reportCreateDto.getReportedEntityType(), reportCreateDto.getReportedEntityId()));
        report.setDescription(reportCreateDto.getDescription());
        report.setReportType(reportCreateDto.getReportType()); // 직접 설정
        report.setReportedMember(reportedMember); // 신고 대상이 USER인 경우 설정

        // 신고 저장
        Report savedReport = reportRepository.save(report);

        // ReportResponseDto 생성 및 반환
        ReportResponseDto createdReportDto = ReportResponseDto.builder()
                .id(savedReport.getReportId())
                .reportType(savedReport.getReportType())
                .reportedEntityType(savedReport.getReportedEntityType())
                .description(savedReport.getDescription())
                .reportedEntityId(savedReport.getReportedEntityId())
                .reportedEntityName(savedReport.getReportedEntityName())
                .reporterMemberId(reporter.getMemberId())
                .build();

        return createdReportDto;
    }


    // 신고 대상의 유효성 검증
    private void validateReportedEntity(ReportedEntityType type, String entityId) {
        switch (type) {
            case ITEM:
                try {
                    Long itemId = Long.parseLong(entityId);
                    if (!itemRepository.existsById(itemId)) {
                        throw new IllegalArgumentException("신고 대상인 아이템이 존재하지 않습니다.");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("아이템 ID는 숫자여야 합니다.");
                }
                break;
            case POST:
                try {
                    Long postId = Long.parseLong(entityId);
                    if (!postRepository.existsById(postId)) {
                        throw new IllegalArgumentException("신고 대상인 게시글이 존재하지 않습니다.");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("게시글 ID는 숫자여야 합니다.");
                }
                break;
            case ORDER:
                try {
                    Long orderId = Long.parseLong(entityId);
                    if (!orderRepository.existsById(orderId)) {
                        throw new IllegalArgumentException("신고 대상인 주문이 존재하지 않습니다.");
                    }
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("거래 ID는 숫자여야 합니다.");
                }
                break;
            case MEMBER:
                if (!memberRepository.existsById(entityId)) {
                    throw new IllegalArgumentException("신고 대상인 사용자가 존재하지 않습니다.");
                }
                break;
            default:
                throw new IllegalArgumentException("유효하지 않은 신고 대상 유형입니다.");
        }
    }


    // 신고 대상의 이름 또는 제목 조회
    private String getReportedEntityName(ReportedEntityType reportedEntityType, String reportedEntityId) {
        switch (reportedEntityType) {
            case ITEM:
                Item item = itemRepository.findById(Long.parseLong(reportedEntityId))
                        .orElse(null);
                return item != null ? item.getItemName() : "Unknown Item";
            case POST:
                Post post = postRepository.findById(Long.parseLong(reportedEntityId))
                        .orElse(null);
                return post != null ? post.getTitle() : "Unknown Post";
            case ORDER:
                Orders order = orderRepository.findById(Long.parseLong(reportedEntityId))
                        .orElse(null);
                return order != null ? "Order #" + order.getOrderId() : "Unknown Order";
            case MEMBER:
                Member member = memberRepository.findByMemberId(reportedEntityId)
                        .orElse(null);
                return member != null ? member.getMemberName() : "Unknown Member";
            default:
                return "Unknown";
        }
    }
}
