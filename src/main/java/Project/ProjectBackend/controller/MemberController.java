package Project.ProjectBackend.controller;

import Project.ProjectBackend.domain.Member;
import Project.ProjectBackend.dto.MemberSignupRequestDto;
import Project.ProjectBackend.dto.MemberUpdateRequestDto;
import Project.ProjectBackend.service.MemberService;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @PostMapping("/members/new")
    public ResponseEntity<?> signup(@RequestBody @Valid MemberSignupRequestDto requestDto) {
        memberService.signup(requestDto); // 회원가입 실행
        return ResponseEntity.ok("회원가입 성공!");
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
        return ResponseEntity.ok(new UpdateMemberResponse(updatedMember.getId(), updatedMember.getName()));
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
                .map(m -> new MemberDto(m.getId(), m.getName())) // ID와 이름만 추출
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

    // 회원 탈퇴




}