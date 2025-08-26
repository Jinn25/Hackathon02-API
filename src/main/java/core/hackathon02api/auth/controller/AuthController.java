package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.LoginRequest;
import core.hackathon02api.auth.dto.LoginResponse;
import core.hackathon02api.auth.dto.UserRegisterRequest;
import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.repository.UserRepository;
import core.hackathon02api.auth.service.AuthService;
import core.hackathon02api.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;     // 회원가입
    private final UserRepository userRepository;
    private final AuthService authService;     // 로그인

    // STEP 1: 아이디 중복검사
    @GetMapping("/check/username")
    public ResponseEntity<Map<String, Object>> checkUsername(@RequestParam("value") String username) {
        String normalized = username == null ? "" : username.trim();
        boolean available = !userRepository.existsByUsername(normalized);
        if (available) {
            return ResponseEntity.ok(Map.of("field","username","available",true));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("field","username","available",false,"message","이미 사용중인 아이디입니다."));
    }

    // STEP 2: 닉네임 중복검사
    @GetMapping("/check/nickname")
    public ResponseEntity<Map<String, Object>> checkNickname(@RequestParam("value") String nickname) {
        String normalized = nickname == null ? "" : nickname.trim();
        boolean available = !userRepository.existsByNickname(normalized);
        if (available) {
            return ResponseEntity.ok(Map.of("field","nickname","available",true));
        }
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("field","nickname","available",false,"message","이미 사용중인 닉네임입니다."));
    }

    // STEP 3: 최종 회원가입
    @PostMapping("/signup")
    public ResponseEntity<User> signup(@RequestBody UserRegisterRequest req) {
        User saved = userService.registerUser(
                safeTrim(req.getUsername()),
                req.getPassword(),
                safeTrim(req.getNickname()),
                normalizeUpper(req.getGender()),
                safeTrim(req.getAgeRange()),
                safeTrim(req.getRoadAddress()),
                req.getInterests()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse resp = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(resp);
    }

    private String safeTrim(String s) { return s == null ? null : s.trim(); }
    private String normalizeUpper(String s) { return s == null ? null : s.trim().toUpperCase(); }
}
