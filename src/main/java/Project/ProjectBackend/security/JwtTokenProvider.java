package Project.ProjectBackend.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import Project.ProjectBackend.entity.Role;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.JwtParserBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class JwtTokenProvider {
    //서명 및 암호화를 위한 SecretKey
    private SecretKey secretKey;
    //AccessToken의 유효 기간(단위: 밀리세컨)
    private long accessTokenDuration = 24*60*60*1000;

    //생성자.  참고로 @Value() {jwt.security.key} 는 application.yml의 정렬에 따라
    public JwtTokenProvider(@Value("${jwt.security.key}") String jwtSecurityKey) {
        try {
            if (jwtSecurityKey == null || jwtSecurityKey.isEmpty()) {
                throw new IllegalArgumentException("JWT Security Key is missing!");
            }
            secretKey = Keys.hmacShaKeyFor(jwtSecurityKey.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("Error initializing JwtTokenProvider: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }


    // AccessToken 생성
    public String createAccessToken(String userId, String role) {
        try {
            // JWT Builder 생성
            JwtBuilder builder = Jwts.builder()
                    .setSubject(userId)  // 사용자 ID 설정
                    .claim("role", role) // 역할 설정
                    .setExpiration(new Date(System.currentTimeMillis() + accessTokenDuration)) // 만료 시간 설정
                    .signWith(secretKey);  // 서명 설정

            // JWT 토큰 생성 및 반환
            return builder.compact();
        } catch (Exception e) {
            log.error("Failed to create access token: {}", e.getMessage());
            throw new JwtTokenCreationException("Error creating JWT token", e);
        }
    }
//    public String createAccessToken(String userId, Role role) {
//        JwtBuilder builder = Jwts.builder()
//                .setSubject(userId)
//                .claim("role", role.name()) // Enum의 name() 사용
//                .setExpiration(new Date(System.currentTimeMillis() + accessTokenDuration))
//                .signWith(secretKey);
//        return builder.compact();
//    }

    public class JwtTokenCreationException extends RuntimeException {
        public JwtTokenCreationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public Jws<Claims> validateToken(String token) {
        Jws<Claims> jws = null;
        try {
            // JWT 파서 빌더 생성
            JwtParserBuilder builder = Jwts.parserBuilder();

            // 비밀 키 설정
            builder.setSigningKey(secretKey);

            // JWT 파서 생성
            JwtParser parser = builder.build();

            // AccessToken으로부터 payload 얻기
            jws = parser.parseClaimsJws(token);
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return jws;
    }


    public String getUserId(Jws<Claims> jws) {
        // Payload 얻기
        Claims claims = jws.getBody();  // getPayload() 대신 getBody() 사용
        // 사용자 아이디 얻기
        return claims.getSubject();  // subject는 기본적으로 사용자 ID로 설정됨
    }

    public String getAuthority(Jws<Claims> jws) {
        // Payload 얻기
        Claims claims = jws.getBody();  // getPayload() 대신 getBody() 사용
        // 사용자 권한 얻기
        String authority = (String) claims.get("role");  // "role"로 변경된 경우 확인
        return authority;
    }

}






