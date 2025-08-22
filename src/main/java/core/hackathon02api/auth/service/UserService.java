package core.hackathon02api.auth.service;

import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public User registerUser(String username, String rawPassword, String nickname,
                             String gender, String ageRange, String roadAddress, String interestsJson) {

        // 아이디 중복 확인
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("이미 사용중인 아이디입니다.");
        }

        // 닉네임 중복 확인
        if (userRepository.existsByNickname(nickname)) {
            throw new RuntimeException("이미 사용중인 닉네임입니다.");
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(rawPassword);

        // 유저 생성
        User user = User.builder()
                .username(username)
                .passwordHash(encodedPassword)
                .nickname(nickname)
                .gender(gender)
                .ageRange(ageRange)
                .roadAddress(roadAddress)
                .interestsJson(interestsJson)
                .build();

        return userRepository.save(user);
    }
}