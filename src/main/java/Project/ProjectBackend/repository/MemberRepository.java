package Project.ProjectBackend.repository;

import Project.ProjectBackend.entity.Member;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, String> {

    Optional<Member> findByEmail(String email);
    Optional<Member> findByMemberId(String memberId);
//    findByMemberId처럼 특정 컬럼을 기반으로 검색하는 메서드는 명시적으로 정의해야 함.
    @Query("SELECT m. profileImageUrl FROM Member m WHERE m.memberId=:memberId")
    String findProfileImageUrl(String memberId);

    // 아이디 중복확인
    boolean existsByMemberId(String memberId);
    // 닉네임 중복확인
    boolean existsByNickName(String nickName);

}
