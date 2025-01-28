package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Report;
import Project.ProjectBackend.entity.ReportType;
import Project.ProjectBackend.entity.ReportedEntityType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    Slice<Report> findByReportedEntityType(ReportedEntityType entityType, Pageable pageable);

}
