package com.pposong.pposongbackend.service;

import com.pposong.pposongbackend.dto.auth.LoginRequest;
import com.pposong.pposongbackend.dto.auth.SignupRequest;
import com.pposong.pposongbackend.entity.User;
import com.pposong.pposongbackend.entity.UserStatus;
import com.pposong.pposongbackend.jwt.JwtProvider;
import com.pposong.pposongbackend.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pposong.pposongbackend.dto.auth.UserResponse;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtProvider jwtProvider
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
    }

    // 회원가입
    @Transactional
    public void signup(SignupRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "이미 사용 중인 이메일입니다."
            );
        }

        String encodedPassword =
                passwordEncoder.encode(request.getPassword());

        User user = new User(
                request.getUsername(),
                request.getEmail(),
                encodedPassword
        );

        userRepository.save(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public String login(LoginRequest request) {

        // 1. 이메일로 회원 조회
        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "이메일 또는 비밀번호가 올바르지 않습니다."
                        )
                );

        // 2. 비밀번호 검사
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new IllegalArgumentException(
                    "이메일 또는 비밀번호가 올바르지 않습니다."
            );
        }

        // 3. 관리자 승인 상태 확인
        if (user.getStatus() != UserStatus.APPROVED) {
            throw new IllegalStateException(
                    "관리자 승인이 필요한 계정입니다."
            );
        }

        // 4. JWT 발급
        return jwtProvider.generateToken(user.getId());
    }

    // 로그인한 사용자 정보 조회
    @Transactional(readOnly = true)
    public UserResponse getMyInfo(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "사용자를 찾을 수 없습니다."
                        )
                );

        return new UserResponse(user);
    }
}