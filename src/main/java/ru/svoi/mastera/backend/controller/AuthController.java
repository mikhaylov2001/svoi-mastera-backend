package ru.svoi.mastera.backend.controller;

import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.svoi.mastera.backend.dto.AuthResponse;
import ru.svoi.mastera.backend.dto.LoginRequest;
import ru.svoi.mastera.backend.dto.RegisterRequest;
import ru.svoi.mastera.backend.dto.UserDto;
import ru.svoi.mastera.backend.service.AuthService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterRequest request){
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @GetMapping("/me")
    public UserDto me(@RequestHeader("X-User-Id") UUID userId) {
        return authService.getMe(userId);
    }

}
