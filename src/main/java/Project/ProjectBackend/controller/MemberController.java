package Project.ProjectBackend.controller;

import Project.ProjectBackend.dto.*;
import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.security.JwtTokenProvider;
import Project.ProjectBackend.service.MemberService;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;


import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class MemberController {

    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;



    // 회원가입, 로그인, 회원정보 수정, 탈퇴

    // 회원가입
    @PostMapping("/members/signup")
    public ResponseEntity<?> signup(
            @RequestPart(value = "memberData") @Valid MemberSignupRequestDto requestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        memberService.signup(requestDto, profileImage);
        return ResponseEntity.ok("회원가입 성공!");
    }



    // 로그인
    @PostMapping("/members/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequest loginRequest) {
        log.debug("로그인 요청: {}", loginRequest); // 💡 실제 들어오는 값 로그 찍기
        // 사용자 인증 (서비스에서 DB 확인)
        Member member = memberService.authenticate(loginRequest.getMemberId(), loginRequest.getPassword());
        if (member == null) {
            return ResponseEntity.status(401).body("Invalid memberId or password");
        }

        // 권한 정보 설정
        String authority = member.getRole().name();
        String token = jwtTokenProvider.createAccessToken(member.getMemberId(), authority);

        return ResponseEntity.ok(new LoginResponse(token, member.getMemberId(), authority));

    }

    // 로그인 - 로그인 요청
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


    // 로그아웃
    @PostMapping("/members/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        log.info("로그아웃 요청 - 토큰: {}", token);
        return ResponseEntity.ok("로그아웃 되었습니다.");
    }


    // 회원정보 수정
    @PatchMapping("/members/update")
    public ResponseEntity<?> updateMember(
            Authentication authentication,
            @RequestPart(value = "memberData") @Valid MemberUpdateRequestDto updateRequestDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

        String memberId = authentication.getName();
        memberService.updateMember(memberId, updateRequestDto, profileImage);

        Member updatedMember = memberService.findOne(memberId);
        return ResponseEntity.ok(new UpdateMemberResponse(updatedMember.getMemberId(), updatedMember.getMemberName()));
    }


    // 회원정보 페이지에서 회원정보 불러오기
    @GetMapping("/members/me")
    public ResponseEntity<?> getMyInfo(Authentication authentication) {
        String memberId = authentication.getName();

        MemberSimpleDto response = memberService.getMemberById(memberId); // ✅ 여기!

        return ResponseEntity.ok(response); // ✅ 닉네임 포함된 DTO 반환
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


    @GetMapping("/members/check-id")
    public ResponseEntity<?> checkDuplicateId(@RequestParam String memberId) {
        if (memberService.existsByMemberId(memberId)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 사용 중인 아이디입니다."));
        }
        return ResponseEntity.ok(Map.of("message", "사용 가능한 아이디입니다."));
    }

    @GetMapping("/members/check-nickname")
    public ResponseEntity<?> checkDuplicateNickName(@RequestParam String nickName) {
        if (memberService.existsByNickName(nickName)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 사용 중인 닉네임입니다."));
        }
        return ResponseEntity.ok(Map.of("message", "사용 가능한 닉네임입니다."));
    }

    @GetMapping("/members/check-phone")
    public ResponseEntity<?> checkDuplicatePhone(@RequestParam String phoneNum) {
        if (memberService.existsByPhoneNum(phoneNum)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 사용 중인 전화번호입니다."));
        }
        return ResponseEntity.ok(Map.of("message", "사용 가능한 전화번호입니다."));
    }

    @GetMapping("/members/check-email")
    public ResponseEntity<?> checkDuplicateEmail(@RequestParam String email) {
        if (memberService.existsByEmail(email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", "이미 사용 중인 이메일입니다."));
        }
        return ResponseEntity.ok(Map.of("message", "사용 가능한 이메일입니다."));
    }
}

