package Project.ProjectBackend.repository;

import Project.ProjectBackend.domain.Member;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByEmail(String email);
    Optional<Member> findByMemberId(String memberId);
//    findByMemberId처럼 특정 컬럼을 기반으로 검색하는 메서드는 명시적으로 정의해야 함.
}
