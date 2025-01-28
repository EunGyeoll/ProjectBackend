package Project.ProjectBackend.service;

import Project.ProjectBackend.dto.*;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.repository.MemberRepository;
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



    // 회원 페이지 (마이페이지) 단건 조회
    @Transactional(readOnly = true)
    public MemberMyPageDto getMyPageData(String targetMemberId, Pageable pageableForItems, Pageable pageableForPosts) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentMemberId = authentication.getName();

        Member member = memberRepository.findByMemberId(targetMemberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        boolean isOwnProfile = currentMemberId.equals(targetMemberId);

        // 아이템 목록 변환
        Slice<ItemResponseDto> itemsSlice = createSlice(
                member.getItems(),
                ItemResponseDto::fromForList,
                pageableForItems
        );

        // 게시글 목록 변환
        Slice<PostResponseDto> postsSlice = createSlice(
                member.getPosts(),
                PostResponseDto::fromForList,
                pageableForPosts
        );

        // 찜한 상품 목록 변환
        Slice<FavoriteItemDto> favoriteItemsSlice = createSlice(
                member.getFavoriteItems(),
                FavoriteItemDto::from,
                pageableForItems // Pageable 필요 시 수정
        );

        // 좋아요한 게시글 목록 변환
        Slice<LikedPostDto> likedPostsSlice = createSlice(
                member.getLikedPosts(),
                LikedPostDto::from,
                pageableForPosts // Pageable 필요 시 수정
        );

        return MemberMyPageDto.from(
                member,
                isOwnProfile,
                itemsSlice.getContent(),
                itemsSlice.hasNext(),
                postsSlice.getContent(),
                postsSlice.hasNext(),
                favoriteItemsSlice.getContent(),
                favoriteItemsSlice.hasNext(),
                likedPostsSlice.getContent(),
                likedPostsSlice.hasNext()
        );
    }



    @Transactional(readOnly = true)
    public MemberMyPageDto getMemberPageData(String memberId, Pageable pageableForItems, Pageable pageableForPosts) {
        // 회원 정보 조회
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원을 찾을 수 없습니다."));

        // 아이템 목록 변환
        Slice<ItemResponseDto> itemsSlice = createSlice(
                member.getItems(),
                ItemResponseDto::fromForList,
                pageableForItems
        );

        // 게시글 목록 변환
        Slice<PostResponseDto> postsSlice = createSlice(
                member.getPosts(),
                PostResponseDto::fromForList,
                pageableForPosts
        );

        // 찜한 상품 목록 변환 (타인의 페이지에서는 빈 목록과 hasNext=false)
        Slice<FavoriteItemDto> favoriteItemsSlice = createSlice(
                new ArrayList<>(), // 타인의 페이지에서는 빈 목록
                FavoriteItemDto::from,
                pageableForItems
        );

        // 좋아요한 게시글 목록 변환 (타인의 페이지에서는 빈 목록과 hasNext=false)
        Slice<LikedPostDto> likedPostsSlice = createSlice(
                new ArrayList<>(), // 타인의 페이지에서는 빈 목록
                LikedPostDto::from,
                pageableForPosts
        );

        // 최종 DTO 반환
        return MemberMyPageDto.from(
                member,
                false, // 자신의 페이지 여부
                itemsSlice.getContent(),
                itemsSlice.hasNext(),
                postsSlice.getContent(),
                postsSlice.hasNext(),
                favoriteItemsSlice.getContent(),
                favoriteItemsSlice.hasNext(),
                likedPostsSlice.getContent(),
                likedPostsSlice.hasNext()
        );
    }



    /**
     * 엔티티 목록을 Slice로 변환하는 유틸리티 메서드
     *
     * @param entities  변환할 엔티티 목록
     * @param mapper    엔티티를 DTO로 변환하는 함수
     * @param pageable  페이징 정보를 포함하는 Pageable 객체
     * @return 변환된 Slice<R> 객체
     */
    private <T, R> Slice<R> createSlice(List<T> entities, Function<T, R> mapper, Pageable pageable) {
        List<R> mappedList = entities.stream()
                .map(mapper)
                .collect(Collectors.toList());

        boolean hasNext = mappedList.size() > pageable.getPageSize();

        if (hasNext) {
            mappedList = mappedList.subList(0, pageable.getPageSize());
        }

        return new SliceImpl<>(mappedList, pageable, hasNext);
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