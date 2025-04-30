package Project.ProjectBackend.security;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.equals("/api/members/signup") || uri.equals("/api/members/login")) {
            filterChain.doFilter(request, response); // 🔥 그냥 통과시킴
            return;
        }

        String method = request.getMethod();

        log.info("[요청 들어옴] URI: {}, Method: {}", uri, method); // 🔥 추가


        String token = getTokenFromHeader(request);
        if (token != null) {
            Jws<Claims> claimsJws = jwtTokenProvider.validateToken(token);
            if (claimsJws != null) {
                Claims claims = claimsJws.getBody();
                String userId = claims.getSubject();
                String role = claims.get("role", String.class); // role 추출

                // 디버깅 로그 추가
                log.debug("JWT 토큰에서 추출한 userId: {}", userId);
                log.debug("JWT 토큰에서 추출한 role: {}", role);

                List<SimpleGrantedAuthority> authorities = role == null ?
                        Collections.emptyList() :
                        List.of(new SimpleGrantedAuthority(role));

                // 권한 로그 추가
                log.debug("부여된 권한: {}", authorities);

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userId, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("SecurityContextHolder에 Authentication 설정 완료: {}",
                        SecurityContextHolder.getContext().getAuthentication());
            }
        }
        filterChain.doFilter(request, response);
    }



    private String getTokenFromHeader(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}
