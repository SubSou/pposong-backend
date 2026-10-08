package com.pposong.pposongbackend.jwt;

import com.pposong.pposongbackend.entity.User;
import com.pposong.pposongbackend.entity.UserStatus;
import com.pposong.pposongbackend.repository.UserRepository;

import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication
        .UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority
        .SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(
            JwtProvider jwtProvider,
            UserRepository userRepository
    ) {
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorization =
                request.getHeader("Authorization");

        // JWT가 없는 경우 다음 필터로 이동
        if (authorization == null ||
                !authorization.startsWith("Bearer ")) {

            filterChain.doFilter(request, response);
            return;
        }

        String token = authorization.substring(7);

        try {
            // JWT 검증 및 사용자 ID 추출
            Long userId = jwtProvider.getUserId(token);

            // 데이터베이스에서 사용자 조회
            User user = userRepository.findById(userId)
                    .orElse(null);

            // 존재하고 승인된 사용자만 인증
            if (user != null &&
                    user.getStatus() == UserStatus.APPROVED) {

                // 사용자 권한 설정
                var authorities = List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_" + user.getRole().name()
                        )
                );

                // 인증 객체 생성
                var authentication =
                        new UsernamePasswordAuthenticationToken(
                                user.getId(),
                                null,
                                authorities
                        );

                // Spring Security에 인증 정보 저장
                SecurityContextHolder.getContext()
                        .setAuthentication(authentication);
            }

        } catch (JwtException | IllegalArgumentException e) {

            // 잘못되었거나 만료된 토큰
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}