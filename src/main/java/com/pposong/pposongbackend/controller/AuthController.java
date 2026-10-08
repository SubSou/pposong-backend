package com.pposong.pposongbackend.controller;

import com.pposong.pposongbackend.dto.auth.LoginRequest;
import com.pposong.pposongbackend.dto.auth.LoginResponse;
import com.pposong.pposongbackend.dto.auth.SignupRequest;
import com.pposong.pposongbackend.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.pposong.pposongbackend.dto.auth.UserResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<Map<String, String>> signup(
            @Valid @RequestBody SignupRequest request
    ) {

        System.out.println("===== 회원가입 요청 들어옴 =====");
        System.out.println("username = " + request.getUsername());
        System.out.println("email = " + request.getEmail());

        authService.signup(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(Map.of(
                        "message",
                        "회원가입이 완료되었습니다. 관리자 승인을 기다려주세요."
                ));
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request
    ) {

        String token = authService.login(request);

        return ResponseEntity.ok(
                new LoginResponse(token)
        );
    }

    // 이메일 중복 및 로그인 정보 오류
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleException(
            IllegalArgumentException e
    ) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        "message",
                        e.getMessage()
                ));
    }

    // 관리자 승인 필요
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleStateException(
            IllegalStateException e
    ) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(Map.of(
                        "message",
                        e.getMessage()
                ));
    }

    // 로그인한 사용자 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(
            @AuthenticationPrincipal Long userId
    ) {
        UserResponse response = authService.getMyInfo(userId);

        return ResponseEntity.ok(response);
    }
}