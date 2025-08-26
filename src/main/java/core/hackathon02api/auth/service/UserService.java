package core.hackathon02api.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    // 회원가입
    public User registerUser(String username, String rawPassword, String nickname,
                             String gender, String ageRange, String roadAddress, List<String> interests) {

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("이미 사용중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new RuntimeException("이미 사용중인 닉네임입니다.");
        }

        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 예외 처리 추가
        String interestsJson = null;
        try {
            if (interests != null) {
                interestsJson = objectMapper.writeValueAsString(interests);
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("관심사 형식이 올바르지 않습니다.", e);
        }

        User user = User.builder()
                .username(username)
                .passwordHash(encodedPassword)
                .nickname(nickname)
                .gender(gender)
                .ageRange(ageRange)
                .roadAddress(roadAddress)
                .interestsJson(interestsJson)
                .build();

        User saved = userRepository.save(user);

        // 응답용 transient 필드 채워주기
        saved.setInterests(interests);
        return saved;
    }
}