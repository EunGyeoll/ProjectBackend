package Project.ProjectBackend.service;

import Project.ProjectBackend.entity.Member;
import Project.ProjectBackend.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthService {

    private final MemberRepository memberRepository;

    public AuthService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Member getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Authentication: {}", authentication);

        if(authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new RuntimeException("인증된 사용자 정보를 가져올 수 없습니다.");
        }

        String userId = authentication.getName();
        log.debug("Authenticated userId: {}", userId);

        Member member = memberRepository.findByMemberId(userId).orElse(null);
        if(member == null) {
            log.debug("Member not found for userId: {}", userId);
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        log.debug("Authenticated Member: {}", member);
        return member;
    }
}
