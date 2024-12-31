package Project.ProjectBackend.service;

import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.dto.MemberSignupRequestDto;
import Project.ProjectBackend.dto.MemberUpdateRequestDto;
import Project.ProjectBackend.repository.MemberRepository;
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

    // 회원가입 (아이디를 반환하도록 되어있음. 회원가입이 완료되었습니다!라는 문구만 반환할거면 반환타입 Long 이 아닌 다른거로 하기.)
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
                .role(requestDto.getRole())
                .phoneNum(requestDto.getPhoneNum())
                .role("ROLE_USER")
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

}
