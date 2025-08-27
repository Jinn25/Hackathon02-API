// core.hackathon02api.auth.service.AuthService
package core.hackathon02api.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.hackathon02api.auth.dto.LoginResponse;
import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.repository.UserRepository;
import core.hackathon02api.auth.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;   // interestsJson 역직렬화용

    public LoginResponse login(String username, String password) {
        // 1) 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 사용자입니다."));

        // 2) 비밀번호 검증
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        // 3) JWT 발급 (subject = userId, roles = USER)
        String token = jwtTokenProvider.createToken(user.getId().toString(),
                Collections.singletonList("ROLE_USER"));

        // 4) interestsJson → List<String>
        List<String> interests = Collections.emptyList();
        try {
            if (user.getInterestsJson() != null) {
                interests = objectMapper.readValue(
                        user.getInterestsJson(),
                        new TypeReference<List<String>>() {}
                );
            }
        } catch (Exception ignored) { /* 필요 시 로깅 */ } //굳이 안 할 듯

        // 5) 응답 DTO에 프로필 포함해 반환
        return new LoginResponse(
                token,
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                user.getRoadAddress(),
                interests
        );
    }
}
