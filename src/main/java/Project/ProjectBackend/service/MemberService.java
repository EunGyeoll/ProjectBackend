package Project.ProjectBackend.service;


import Project.ProjectBackend.config.S3Properties;
import Project.ProjectBackend.dto.*;
import Project.ProjectBackend.entity.Address;
import Project.ProjectBackend.entity.Image;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.repository.*;
import Project.ProjectBackend.entity.Role;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private static final Logger logger = LoggerFactory.getLogger(MemberService.class);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageService imageService;
    private final S3Properties s3Properties;


    // 회원가입
    @Transactional
    public void signup(MemberSignupRequestDto requestDto, MultipartFile profileImage) {
        // 이메일 중복 체크
        validateDuplicateEmail(requestDto.getEmail());

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        // AddressDto → Address 변환
        Address address = new Address(
                requestDto.getAddress().getMainAddress(),
                requestDto.getAddress().getDetailAddress(),
                requestDto.getAddress().getZipcode()
        );

        // DTO를 엔티티로 변환 (우선적으로 Member 엔티티 생성)
        Member member = Member.builder()
                .memberId(requestDto.getMemberId())
                .memberName(requestDto.getMemberName())
                .nickName(requestDto.getNickName())
                .email(requestDto.getEmail())
                .password(encodedPassword)
                .address(address) // 변환된 address 넣기
                .birthDate(requestDto.getBirthDate())
                .role(Role.ROLE_USER)
                .phoneNum(requestDto.getPhoneNum())
                .enabled(true)
                .build();

        // 프로필 이미지 저장 (프로필 이미지가 있을 경우)
        if (profileImage != null && !profileImage.isEmpty()) {
            Image savedProfileImage = imageService.saveImageForProfile(profileImage, member);
            member.setProfileImageUrl(savedProfileImage.getImagePath());  // URL 저장
        }

        // 프로필 이미지 경로 업데이트 했으니 다시 저장
        memberRepository.save(member);
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

            // ✅ 디버깅용 출력
            System.out.println("입력 비번: " + password);
            System.out.println("DB 비번: " + member.getPassword());
            System.out.println("matches? " + passwordEncoder.matches(password, member.getPassword()));


            if (passwordEncoder.matches(password, member.getPassword())) {
                return member;
            }

        }
        return null;
    }

    // 회원 정보 수정
    @Transactional
    public void updateMember(String memberId, MemberUpdateRequestDto updateRequestDto, MultipartFile profileImage, boolean isImageRemoved) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new EntityNotFoundException("회원 정보를 찾을 수 없습니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(updateRequestDto.getCurrentPassword(), member.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        // 기본 필드 업데이트
        if (updateRequestDto.getName() != null) member.setMemberName(updateRequestDto.getName());
        if (updateRequestDto.getEmail() != null) member.setEmail(updateRequestDto.getEmail());
        if (updateRequestDto.getNickName() != null) member.setNickName(updateRequestDto.getNickName());
        if (updateRequestDto.getBirthDate() != null) member.setBirthDate(updateRequestDto.getBirthDate());
        if (updateRequestDto.getAddress() != null) member.setAddress(updateRequestDto.getAddress());

        // 이미지 제거 요청 + 새 업로드 없음 → 기본 이미지로 설정
        if (isImageRemoved && (profileImage == null || profileImage.isEmpty())) {
            if (member.getProfileImage() != null) {
                imageService.deleteImage(member.getProfileImage());
            }

            member.setProfileImage(null);
            member.updateProfileImage(s3Properties.getDefaultProfileUrl());

            logger.info("기본 프로필 이미지로 설정됨");
        }

        // 새 프로필 이미지 업로드
        else if (profileImage != null && !profileImage.isEmpty()) {
            if (member.getProfileImage() != null) {
                imageService.deleteImage(member.getProfileImage());
            }

            Image newProfileImage = imageService.saveImageForProfile(profileImage, member);
            member.setProfileImage(newProfileImage);
            member.updateProfileImage(newProfileImage.getImagePath());

            logger.info("새 프로필 이미지 설정됨: " + newProfileImage.getImagePath());
        }

        // 비밀번호 변경
        if (StringUtils.hasText(updateRequestDto.getNewPassword())) {
            member.updatePassword(passwordEncoder.encode(updateRequestDto.getNewPassword()));
        }

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

        return MemberSimpleDto.from(member); // ✅ 이 한 줄로 교체
    }


    public boolean existsByMemberId(String memberId) {
        return memberRepository.existsByMemberId(memberId);
    }

    public boolean existsByNickName(String nickName) {
        return memberRepository.existsByNickName(nickName);
    }

    public boolean existsByEmail(String email) {
        return memberRepository.existsByEmail(email);
    }

    public boolean existsByPhoneNum(String phoneNum) {
        return memberRepository.existsByPhoneNum(phoneNum);
    }

}