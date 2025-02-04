package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.*;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.repository.*;
import Project.ProjectBackend.entity.Role;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ItemRepository itemRepository;
    private final PostRepository postRepository;
    private final FavoriteRepository favoriteRepository;
    private final LikedPostRepository likedPostRepository;


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
    public void updateMember(String memberId, MemberUpdateRequestDto updateRequestDto) {
        // 기존 회원 정보 조회
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 전달된 값만 업데이트
        if (updateRequestDto.getPassword() != null) {
            String encodedPassword = passwordEncoder.encode(updateRequestDto.getPassword());
            member.setPassword(encodedPassword);
        }
        if (updateRequestDto.getName() != null) {
            member.setName(updateRequestDto.getName());
        }
        if (updateRequestDto.getEmail() != null) {
            member.setEmail(updateRequestDto.getEmail());
        }
        if (updateRequestDto.getBirthDate() != null) {
            member.setBirthDate(updateRequestDto.getBirthDate());
        }
        if (updateRequestDto.getAddress() != null) {
            member.setAddress(updateRequestDto.getAddress());
        }

        // 업데이트된 데이터를 저장
        memberRepository.save(member);
    }


    public Member findOne(String id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("회원이 존재하지 않습니다. ID: " + id));
    }


    // 회원 목록 조회
    @Transactional
    public List<Member> findMembers() {
        return memberRepository.findAll();
    }




    // 회원 탈퇴 (enabled 필드를 false로 설정)
    @Transactional
    public boolean deleteMember(String memberId) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        if(member != null && member.isEnabled()) {
            member.setEnabled(false); // Soft delete

            memberRepository.save(member);
            return true;
        }
        return false;
    }


    //비밀번호 확인 메서드
    public boolean checkPassword(String memberId, String rawPassword) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));
        return passwordEncoder.matches(rawPassword, member.getPassword());
    }


    @Transactional(readOnly = true)
    public MemberSimpleDto getMemberById(String memberId) {

        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        return MemberSimpleDto.builder()
                .memberId(member.getMemberId())
                .name(member.getName())
                .email(member.getEmail())
                .role(member.getRole())
                .enabled(member.isEnabled())
                .birthDate(member.getBirthDate())
                .phoneNum(member.getPhoneNum())
                .address(AddressDto.from(member.getAddress()))
                .build();
    }
}