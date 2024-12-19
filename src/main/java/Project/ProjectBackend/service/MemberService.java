package Project.ProjectBackend.service;

import Project.ProjectBackend.domain.Member;

import Project.ProjectBackend.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {


    private final MemberRepository memberRepository;


    // 회원가입
    public Long join(Member member) {
        validateDuplicateMember(member); //중복 회원 검증
        memberRepository.save(member);
        return member.getId();
    }

    private void validateDuplicateMember(Member member) {
        List<Member> findMembers = memberRepository.findByName(member.getName());
        if (!findMembers.isEmpty()) {
            throw new IllegalStateException("이미 존재하는 회원입니다.");
        }
    }

    // 회원 전체 조회
    @Transactional(readOnly = true)
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }

    //회원 선택 조회
    @Transactional(readOnly = true)
    public Member findOne(Long memberId) {
        return memberRepository.findOne(memberId);
    }

    
    // 회원 수정
    @Transactional
    public void update(Long id, String name) {
        Member member = memberRepository.findOne(id); // 영속성 컨텍스트에서 id를 찾으면 없을테니 db에서 찾아올 것이다.
        member.setName(name); // 영속상태인 member를 setName으로 바꿔줌
    }
}
