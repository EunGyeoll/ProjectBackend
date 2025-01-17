package Project.ProjectBackend.controller;

import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.dto.MemberSignupRequestDto;
import Project.ProjectBackend.dto.MemberUpdateRequestDto;
import Project.ProjectBackend.repository.MemberRepository;
import Project.ProjectBackend.security.JwtTokenProvider;
import Project.ProjectBackend.service.MemberService;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider; // @RequiredArgsConstructor를 사용했기 때문에 자동으로 final 필드에 대한 생성자가 생성됨
    private final MemberRepository memberRepository;

//    public MemberController(JwtTokenProvider jwtTokenProvider) {
//        this.jwtTokenProvider = jwtTokenProvider;
//    }

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

        public String getToken() {
            return token;
        }
    }

    // 회원정보 수정
    @PutMapping("/members/update/{id}")
    public ResponseEntity<?> updateMember(
            // 수정하고 나서도 원래의 비밀번호 들어가야 한다.
            @PathVariable("id") String id,
            @RequestBody @Valid MemberUpdateRequestDto updateRequestDto) {

        // 회원 정보 업데이트
        memberService.update(id, updateRequestDto);

        // 업데이트된 회원 정보 반환
        Member updatedMember = memberService.findOne(id);
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


    // 회원 전체 목록 조회
    @GetMapping("/members/list")
    public Result<List<MemberDto>> getMembers() {
        List<Member> findMembers = memberService.findMembers();

        // 엔티티 -> DTO 변환
        List<MemberDto> collect = findMembers.stream()
                .map(m -> new MemberDto(m.getMemberId(), m.getName())) // ID와 이름만 추출
                .collect(Collectors.toList());

        return new Result<>(collect); // 래핑하여 반환
    }

    @Data
    @AllArgsConstructor
    static class Result<T> {
        private T data;
    }

    @Data
    @AllArgsConstructor
    static class MemberDto {
        private String name;
        private String id;
    }


    // 회원 탈퇴  (헤더에  JWT 토큰 싣고 password를 재확인하여 탈퇴)
    @DeleteMapping("/members/delete")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> deleteMember(Authentication authentication, @RequestBody @Valid DeleteMemberRequest deleteRequest) {
        String currentMemberId = authentication.getName(); // 인증된 사용자 ID 가져오기

        // 비밀번호 확인
        boolean isPasswordValid = memberService.checkPassword(currentMemberId, deleteRequest.getPassword());
        if (!isPasswordValid) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("회원을 찾을 수 없습니다.");
        }

        // 회원 탈퇴 실행
        memberService.deleteMember(currentMemberId);

        return ResponseEntity.ok("회원 탈퇴가 완료되었습니다.");
    }

    @Data
    static class DeleteMemberRequest {
        @NotEmpty
        private String password;
    }


    @PutMapping("/members/restore-role/{id}")
//    @PreAuthorize("hasRole('ADMIN')") // 관리자 권한만 허용
    public ResponseEntity<?> restoreRole(@PathVariable("id") String memberId) {
        // 회원 찾기
        Member member = memberService.findOne(memberId);
        if (member == null) {
            return ResponseEntity.status(404).body("회원을 찾을 수 없습니다.");
        }

        // 권한 복구 (기본값으로 ROLE_USER 복구)
        memberService.restoreRole(memberId, "ROLE_USER");

        return ResponseEntity.ok("회원 권한이 복구되었습니다.");
    }



}