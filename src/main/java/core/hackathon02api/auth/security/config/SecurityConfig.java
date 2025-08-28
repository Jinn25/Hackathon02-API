package core.hackathon02api.auth.security.config;

import core.hackathon02api.auth.security.jwt.JwtAuthenticationFilter;
import core.hackathon02api.auth.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
                .cors(cors -> cors.configurationSource(request -> {
                    CorsConfiguration config = new CorsConfiguration();
                    // ✅ 패턴 기반으로 로컬/배포 모두 허용
                    config.setAllowedOriginPatterns(List.of(
                            "http://localhost:*",
                            "http://127.0.0.1:*",
                            "https://*.railway.app",
                            "https://*.github.io",
                            "https://dododokk.github.io" // 필요 시 정확한 배포 프론트 도메인
                    ));
                    config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
                    config.setAllowedHeaders(List.of("*")); // 여기서 Authorization, Content-Type만 열지 말고 전부 허용
                    config.setExposedHeaders(List.of("Authorization", "Location", "Content-Disposition"));
                    config.setAllowCredentials(true);
                    return config;
                }))
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**", "/actuator/health").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/posts/**").permitAll()
                        .requestMatchers("/api/posts/*/applications/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/ws/**").permitAll() // ✅ 웹소켓/ SockJS info 등 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers("/api/notifications/**").authenticated()
                        .requestMatchers("/api/chatrooms/*/read-cursor").authenticated()
                        .anyRequest().permitAll()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            res.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
                            res.setHeader("Access-Control-Allow-Credentials", "true");
                        })
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            res.setHeader("Access-Control-Allow-Origin", req.getHeader("Origin"));
                            res.setHeader("Access-Control-Allow-Credentials", "true");
                        })
                );

        http.addFilterBefore(new JwtAuthenticationFilter(jwtTokenProvider),
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public org.springframework.security.crypto.password.PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }
}
