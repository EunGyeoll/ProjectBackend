package Project.ProjectBackend.websocket;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // stomp 접속 주소 url = ws://localhost:8080/ws/chat 즉, 프로토콜이 http가 아니다.
        registry.addEndpoint("/ws/chat")
                .setAllowedOrigins("*");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지를 받는(구독) 엔드포인트
        registry.enableSimpleBroker("/sub");

        // 메시지를 보내는(발행) 엔드포인트
        registry.setApplicationDestinationPrefixes("/pub");

        // 특정 사용자에게 메시지 전송
        registry.setUserDestinationPrefix("/user");

    }


}
