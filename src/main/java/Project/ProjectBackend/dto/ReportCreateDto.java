package Project.ProjectBackend.dto;

import Project.ProjectBackend.entity.ReportType;
import Project.ProjectBackend.entity.ReportedEntityType;
import lombok.Data;

@Data
public class ReportCreateDto {
    private ReportType reportType; // 신고 유형 (SPAM, HARASSMENT 등)
    private String description; // 신고 설명
    private ReportedEntityType reportedEntityType; // 신고 대상 유형 (ITEM, POST 등)
    private String reportedEntityId; // 신고 대상 ID
}
