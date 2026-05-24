package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.LoginRequest;
import com.company.pcmgmt.api.dto.response.LoginResponse;
import com.company.pcmgmt.domain.entity.User;
import com.company.pcmgmt.domain.mapper.UserMapper;
import com.company.pcmgmt.security.JwtTokenProvider;
import com.company.pcmgmt.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final UserDetailsServiceImpl userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        // ユーザー存在確認
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            throw new BadCredentialsException("ユーザー名またはパスワードが正しくありません");
        }

        // パスワード検証
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadCredentialsException("ユーザー名またはパスワードが正しくありません");
        }

        // JWT生成
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        String token = jwtTokenProvider.generateToken(userDetails, user.getId(), user.getRole());

        // 最終ログイン日時を更新
        userMapper.updateLastLoginAt(user.getId());

        log.info("ログイン成功: username={}, role={}", user.getUsername(), user.getRole());

        return LoginResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(expiration / 1000)
                .userId(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .role(user.getRole())
                .build();
    }
}
