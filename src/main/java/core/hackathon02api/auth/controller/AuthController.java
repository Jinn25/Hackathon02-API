package core.hackathon02api.auth.controller;

import core.hackathon02api.auth.dto.LoginRequest;
import core.hackathon02api.auth.dto.LoginResponse;
import core.hackathon02api.auth.dto.UserRegisterRequest;
import core.hackathon02api.auth.entity.User;
import core.hackathon02api.auth.service.AuthService;
import core.hackathon02api.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor

public class AuthController {
    private final UserService userService;

    private final AuthService authService;  // ✅ 이제 인식됨

    @PostMapping("/signup")
    public ResponseEntity<User> register(@RequestBody UserRegisterRequest request) {
        User user = userService.registerUser(
                request.getUsername(),
                request.getPassword(),
                request.getNickname(),
                request.getGender(),
                request.getAgeRange(),
                request.getRoadAddress(),
                request.getInterestsJson()
        );
        return ResponseEntity.ok(user);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        String token = authService.login(request.getUsername(), request.getPassword());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}
