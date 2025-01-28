package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.ReportType;
import Project.ProjectBackend.entity.ReportedEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ReportResponseDto {
    private Long id; // 신고 고유 ID
    private ReportType reportType; // 신고 유형 (ITEM, POST, TRANSACTION 등)
    private ReportedEntityType reportedEntityType; // 신고 이유
    private String description; // 신고 상세 설명
    private String reportedEntityId; // 신고 대상 ID
    private String reportedEntityName; // 신고 대상 ID
    private String reporterMemberId; // 신고자 회원 ID (응답 시 포함)


}