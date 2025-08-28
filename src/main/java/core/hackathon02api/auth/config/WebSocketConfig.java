package core.hackathon02api.auth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
//    @Override
//    public void registerStompEndpoints(StompEndpointRegistry registry) {
////        registry.addEndpoint("/ws")
////                .setAllowedOriginPatterns("*"); // CORS 허용
////                .withSockJS();
////        // .withSockJS(); ← 웹에서는 SockJS 쓸 수도 있음
//
//        registry.addEndpoint("/ws")
//                .setAllowedOriginPatterns("http://localhost:5500", "http://127.0.0.1:5500") // 허용할 CORS 출처
//                .withSockJS(); // SockJS fallback 지원
//    }
//
//    @Override
//    public void configureMessageBroker(MessageBrokerRegistry registry) {
//        registry.enableSimpleBroker("/sub");
//        registry.setApplicationDestinationPrefixes("/pub");
//    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "https://*.railway.app",
                        "https://*.github.io",
                        "https://YOUR_FRONT_DOMAIN"
                )
                .withSockJS(); // SockJS fallback 활성화
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub");
        registry.setApplicationDestinationPrefixes("/pub");
    }
}
