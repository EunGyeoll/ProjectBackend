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

    // 닉네임 조회
    @Query("SELECT m.nickName FROM Member m WHERE m.memberId = :memberId")
    String findNicknameByMemberId(@Param("memberId") String memberId);

    //프로필 이미지 url 조회
    @Query("SELECT m. profileImageUrl FROM Member m WHERE m.memberId=:memberId")
    String findProfileImageUrl(@Param("memberId") String memberId);

    // 아이디 중복확인
    boolean existsByMemberId(String memberId);
    // 닉네임 중복확인
    boolean existsByNickName(String nickName);
    // 이메일 중복확인
    boolean existsByEmail(String email);
    // 폰번호 중복확인
    boolean existsByPhoneNum(String phoneNum);


}
