package Project.ProjectBackend.controller;


import Project.ProjectBackend.dto.ReportCreateDto;
import Project.ProjectBackend.dto.ReportResponseDto;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.service.AdminService;
import Project.ProjectBackend.service.AuthService;
import Project.ProjectBackend.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;
    private final AuthService authService;
    private final AdminService adminService;

    // 신고 생성
    @PreAuthorize("hasAnyAuthority('ROLE_USER') or hasAnyAuthority('ROLE_ADMIN')")
    @PostMapping("/reports")
    public ResponseEntity<ReportResponseDto> createReport(
            @RequestBody @Valid ReportCreateDto reportCreateDto) {

        // 현재 로그인된 사용자 추출
        Member currentUser = authService.getCurrentUser();

        // 신고 생성 로직
        ReportResponseDto createdReport = reportService.createReport(reportCreateDto, currentUser);
        return ResponseEntity.ok(createdReport);
    }


}
