package Project.ProjectBackend.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import Project.ProjectBackend.security.JwtTokenProvider;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Claims;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            // 🔥 WebSocket 요청 URL에서 토큰 추출
            String token = servletRequest.getServletRequest().getParameter("token");

            if (token != null) {
                Jws<Claims> jws = jwtTokenProvider.validateToken(token);
                if (jws != null) {
                    String memberId = jwtTokenProvider.getmemberId(jws);
                    attributes.put("memberId", memberId); // 🔥 WebSocket 세션에 memberId 저장
                    log.info("✅ WebSocket 연결 성공 - 사용자 ID: {}", memberId);
                    return true;
                }
            }
        }

        log.warn("⛔ WebSocket 연결 실패 - JWT 검증 실패");
        return false; // JWT 검증 실패 → WebSocket 연결 거부
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
    }
}