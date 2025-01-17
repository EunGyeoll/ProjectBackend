package Project.ProjectBackend.service;

import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.dto.MemberSignupRequestDto;
import Project.ProjectBackend.dto.MemberUpdateRequestDto;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public String signup(MemberSignupRequestDto requestDto) {
        // 이메일 중복 체크
        validateDuplicateEmail(requestDto.getEmail());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // DTO를 엔티티로 변환
        Member member = Member.builder()
                .memberId(requestDto.getMemberId()) // 클라이언트로부터 받은 ID 설정
                .name(requestDto.getName())
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .address(requestDto.getAddress())
                .birthDate(requestDto.getBirthDate())
                .role(Role.ROLE_USER)
                .phoneNum(requestDto.getPhoneNum())
                .enabled(true) // 기본값으로 true 설정
                .build();

        // 회원 저장
        Member savedMember = memberRepository.save(member);
        return savedMember.getMemberId();
    }

    private void validateDuplicateEmail(String email) {
        memberRepository.findByEmail(email)
                .ifPresent(m -> {
                    throw new IllegalStateException("이미 존재하는 이메일입니다.");
                });
    }


    // authenticate
    public Member authenticate(String memberId, String password) {
        Optional<Member> optionalMember = memberRepository.findByMemberId(memberId);
        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            if (passwordEncoder.matches(password, member.getPassword())) {
                return member;
            }
        }
        return null;
    }

    // 회원 정보 수정
    @Transactional
    public void update(String id, MemberUpdateRequestDto updateRequestDto) {
        // 기존 회원 조회
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원이 존재하지 않습니다. ID: " + id));

        // 업데이트할 정보 설정
        member.setName(updateRequestDto.getName()); // 이름 변경
        member.setEmail(updateRequestDto.getEmail()); // 이메일 변경

        if (updateRequestDto.getPassword() != null && !updateRequestDto.getPassword().isEmpty()) {
            String encodedPassword = passwordEncoder.encode(updateRequestDto.getPassword());
            member.setPassword(encodedPassword); // 비밀번호 변경
        }


        if (updateRequestDto.getAddress() != null) {
            member.setAddress(updateRequestDto.getAddress()); // 주소 변경
        }
    }

    public Member findOne(String id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. ID: " + id));
    }

    // 회원 목록 조회
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }


    /**
     * 비밀번호 확인 메서드
     */
    public boolean checkPassword(String memberId, String rawPassword) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }

    // 회원 탈퇴
    @Transactional
    public void deleteMember(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        // enabled 필드를 false로 설정
        member.setEnabled(false);
        memberRepository.save(member);

        // 아예 member 정보 지우려면 아래 주석 해제
        // memberRepository.delete(member);
    }

    // 회원 권한 복구
    public void restoreRole(String memberId, String role) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다."));
        member.setEnabled(true);
        memberRepository.save(member); // 변경 사항 저장
    }

}