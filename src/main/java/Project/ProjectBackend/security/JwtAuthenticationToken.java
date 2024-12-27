package Project.ProjectBackend.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {
    private final Object principal; // 사용자 정보 (예: userId)
    private Object credentials;     // 자격 증명 정보 (예: JWT 토큰)

    public JwtAuthenticationToken(Object principal, Object credentials) {
        super(null); // 인증되지 않은 상태
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(false); // 초기에는 인증되지 않은 상태
    }

    public JwtAuthenticationToken(Object principal, Object credentials, Collection<? extends GrantedAuthority> authorities) {
        super(authorities); // 인증된 상태
        this.principal = principal;
        this.credentials = credentials;
        setAuthenticated(true); // 인증된 상태로 설정
    }

    @Override
    public Object getCredentials() {
        return credentials; // JWT 토큰 반환
    }

    @Override
    public Object getPrincipal() {
        return principal; // 사용자 ID 반환
    }
}
