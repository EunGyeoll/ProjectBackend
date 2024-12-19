    package Project.ProjectBackend.controller;

    import Project.ProjectBackend.domain.Member;
    import Project.ProjectBackend.service.MemberService;

    import jakarta.validation.Valid;
    import lombok.Data;
    import lombok.RequiredArgsConstructor;
    import org.springframework.web.bind.annotation.PostMapping;
    import org.springframework.web.bind.annotation.RequestBody;
    import org.springframework.web.bind.annotation.RestController;

    @RestController
    @RequiredArgsConstructor
    public class MemberController {

        private final MemberService memberService;

        @PostMapping("/members/new")
        public CreateMemberResponse saveMember(@RequestBody @Valid CreateMemberRequest request) {

            Member member = new Member();
            member.setName(request.getName());

            Long id = memberService.join(member);
            return new CreateMemberResponse(id);
        }

        @Data
        static class CreateMemberResponse {
            private Long id;

            public CreateMemberResponse(Long id) {
                this.id = id;
            }
        }

        @Data
        static class CreateMemberRequest {
            private String name;
        }
    }
