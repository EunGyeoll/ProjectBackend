package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.*;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.security.JwtTokenProvider;
import Project.ProjectBackend.service.MemberService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;


import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberRepository memberRepository;

    // 회원가입, 로그인, 회원정보 수정, 회원 페이지(마이페이지) 조회, 탈퇴


    // 회원가입
    @PostMapping("/members/new")
    public ResponseEntity<?> signup(@RequestBody @Valid MemberSignupRequestDto requestDto) {
        memberService.signup(requestDto); // 회원가입 실행
        return ResponseEntity.ok("회원가입 성공!");
    }


    // 로그인
    @PostMapping("/members/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        // 사용자 인증 (서비스에서 DB 확인)
        Member member = memberService.authenticate(loginRequest.getMemberId(), loginRequest.getPassword());
        if (member == null) {
            return ResponseEntity.status(401).body("Invalid memberId or password");
        }

        // 권한 정보 설정
        String authority = member.getRole().name(); // ROLE_USER 여야 함.
        String token = jwtTokenProvider.createAccessToken(member.getMemberId(), authority);

        return ResponseEntity.ok(new AuthResponse(token));
    }


    @Data
    static class LoginRequest {
        private String memberId;
        private String password;
    }

    @Data
    static class AuthResponse {
        private String token;

        public AuthResponse(String token) {
            this.token = token;
        }
    }



    // 회원정보 수정
    @PutMapping("/members/update/{memberId}")
    public ResponseEntity<?> updateMember(
            // 수정하고 나서도 원래의 비밀번호 들어가야 한다.
            @PathVariable String memberId,
            @RequestBody @Valid MemberUpdateRequestDto updateRequestDto) {

        // 회원 정보 업데이트
        memberService.updateMember(memberId, updateRequestDto);

        // 업데이트된 회원 정보 반환
        Member updatedMember = memberService.findOne(memberId);
        return ResponseEntity.ok(new UpdateMemberResponse(updatedMember.getMemberId(), updatedMember.getName()));
    }

     // 응답 DTO
        @Data
        static class UpdateMemberResponse {
            private String id;
            private String name;

            public UpdateMemberResponse(String id, String name) {
                this.id = id;
                this.name = name;
            }
        }



    // 회원 페이지 단건 조회 (누구나 조회 가능 / 자신의 페이지일 경우에만 name, address, phoneNum 같은 상세한 개인정보 조회 & 수정 버튼 표시 )
    @GetMapping("/members/{memberId}")
    public ResponseEntity<MemberMyPageDto> getMemberPage(
            @PathVariable String memberId,
            @RequestParam(defaultValue = "0") int itemsPage,
            @RequestParam(defaultValue = "10") int itemsSize,
            @RequestParam(defaultValue = "latest") String itemsSortOption,
            @RequestParam(defaultValue = "0") int postsPage,
            @RequestParam(defaultValue = "10") int postsSize,
            @RequestParam(defaultValue = "latest") String postsSortOption,
            @RequestParam(defaultValue = "0") int favoriteItemsPage,
            @RequestParam(defaultValue = "10") int favoriteItemsSize,
            @RequestParam(defaultValue = "latest") String favoriteItemsSortOption,
            @RequestParam(defaultValue = "0") int likedPostsPage,
            @RequestParam(defaultValue = "10") int likedPostsSize,
            @RequestParam(defaultValue = "latest") String likedPostsSortOption
            ) {

        // 현재 로그인한 사용자 ID
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentMemberId = authentication.getName();

        // 조회 대상과 현재 사용자가 동일한지 확인
        boolean isOwner = currentMemberId.equals(memberId);

        // 아이템
        Sort itemsSortOrder = getSortOrder(itemsSortOption, "item");
        Pageable pageableForItems = PageRequest.of(itemsPage, itemsSize, itemsSortOrder);

        // 포스트
        Sort postsSortOrder = getSortOrder(postsSortOption, "post");
        Pageable pageableForPosts = PageRequest.of(postsPage, postsSize, postsSortOrder);

        // 찜한 아이템
        Sort favoriteItemsSortOrder = getSortOrder(favoriteItemsSortOption, "favoriteItem");
        Pageable pageableForFavoriteItems = PageRequest.of(favoriteItemsPage, favoriteItemsSize, favoriteItemsSortOrder);

        // 좋아요한 포스트
        Sort likedPostsSortOrder = getSortOrder(likedPostsSortOption, "likedPost");
        Pageable pageableForLikedPosts = PageRequest.of(likedPostsPage, likedPostsSize, likedPostsSortOrder);

        if (isOwner) {
            // 자신의 페이지 데이터 조회
            MemberMyPageDto myPageData = memberService.getMyPageData(memberId, pageableForItems, pageableForPosts, pageableForFavoriteItems, pageableForLikedPosts);
            return ResponseEntity.ok(myPageData);
        } else {
            // 다른 사용자의 페이지 데이터 조회
            MemberMyPageDto memberPageData = memberService.getMemberPageData(memberId, pageableForItems, pageableForPosts);
            return ResponseEntity.ok(memberPageData);
        }
    }



    // 회원 탈퇴  (헤더에  JWT 토큰 싣고 password를 재확인하여 탈퇴)
    @DeleteMapping("/members/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMember(Authentication authentication, @RequestBody @Valid DeleteMemberRequest deleteRequest) {
        String currentMemberId = authentication.getName(); // 인증된 사용자 ID 가져오기

        // 비밀번호 확인
        boolean isPasswordValid = memberService.checkPassword(currentMemberId, deleteRequest.getPassword());
        if (!isPasswordValid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("비밀번호가 일치하지 않습니다.");
        }

        boolean isDeleted = memberService.deleteMember(deleteRequest.getPassword());

        if(isDeleted){
            return ResponseEntity.ok("회원 탈퇴 처리가 완료되었습니다.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("회원을 찾을 수 없습니다.");
        }

    }

    @Data
    static class DeleteMemberRequest {
        @NotEmpty(message = "비밀번호는 필수 입력 항목입니다.")
        private String password;
    }



    // 정렬 옵션에 따른 Sort 객체 생성
    private Sort getSortOrder(String sortOption, String entityType) {
        switch (entityType.toLowerCase()) {
            case "item":
                switch (sortOption.toLowerCase()) {
                    case "popular":
                        return Sort.by(Sort.Direction.DESC, "favoriteCount");
                    case "lowprice":
                        return Sort.by(Sort.Direction.ASC, "price");
                    case "highprice":
                        return Sort.by(Sort.Direction.DESC, "price");
                    case "latest":
                    default:
                        return Sort.by(Sort.Direction.DESC, "itemDate"); // 최신 아이템 정렬
                }
            case "post":
                switch (sortOption.toLowerCase()) {
                    case "popular":
                        return Sort.by(Sort.Direction.DESC, "likeCount");
                    case "latest":
                    default:
                        return Sort.by(Sort.Direction.DESC, "postDate"); // 최신 게시글 정렬
                }
            case "favoriteItem":
            case "likedPost":
                return Sort.by(Sort.Direction.DESC, "createdAt"); // 찜한 상품 및 좋아요한 게시글 최신순
            default:
                return Sort.by(Sort.Direction.DESC, "createdAt"); // 기본 정렬
        }
    }

}

