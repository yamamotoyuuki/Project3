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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 認証サービス
 *
 * <p>ログイン認証処理を担当する。
 * ユーザー名・パスワードの検証 → JWT トークン生成 → 最終ログイン日時更新 の流れで処理する。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    /** ユーザー情報の DB アクセスを担うマッパー */
    private final UserMapper userMapper;

    /** Spring Security のユーザー情報サービス（JWT 生成時の UserDetails 取得用） */
    private final UserDetailsServiceImpl userDetailsService;

    /** JWT トークンの生成・検証コンポーネント */
    private final JwtTokenProvider jwtTokenProvider;

    /** BCrypt パスワード検証に使用するエンコーダー */
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;

    /** JWT トークンの有効期限（ミリ秒、application.yml から注入） */
    @Value("${jwt.expiration}")
    private Long expiration;

    /**
     * ログイン認証を行い JWT トークンを発行する
     *
     * <p>処理フロー:
     * <ol>
     *   <li>ユーザー名でユーザーを検索（存在しない場合はエラー）</li>
     *   <li>パスワードを BCrypt 検証（不一致の場合はエラー）</li>
     *   <li>JWT トークンを生成</li>
     *   <li>最終ログイン日時を現在時刻に更新</li>
     * </ol>
     * </p>
     *
     * @param request ログインリクエスト（ユーザー名・パスワード）
     * @return ログインレスポンス（JWT トークン・ユーザー情報）
     * @throws BadCredentialsException ユーザー名またはパスワードが不正な場合
     */
    @Transactional
    public LoginResponse login(LoginRequest request) {
        // ユーザー名でDB検索（存在確認）
        User user = userMapper.findByUsername(request.getUsername());
        if (user == null) {
            // ユーザーが存在しない場合（セキュリティのためユーザー名/パスワードどちらが誤りかは明示しない）
            throw new BadCredentialsException("ユーザー名またはパスワードが正しくありません");
        }

        // 入力パスワードをDBのハッシュ値と照合
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            // パスワードが不一致の場合（セキュリティのためユーザー名/パスワードどちらが誤りかは明示しない）
            throw new BadCredentialsException("ユーザー名またはパスワードが正しくありません");
        }

        // Spring Security の UserDetails を取得（JWT 生成に必要）
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        // JWT トークンを生成（ユーザー名・ユーザーID・ロールをクレームに含める）
        String token = jwtTokenProvider.generateToken(userDetails, user.getId(), user.getRole());

        // 最終ログイン日時を現在日時に更新
        userMapper.updateLastLoginAt(user.getId());

        log.info("ログイン成功: username={}, role={}", user.getUsername(), user.getRole());

        // ログインレスポンスを組み立てて返す
        return LoginResponse.builder()
                .token(token)                       // 発行済み JWT トークン
                .tokenType("Bearer")               // トークン種別（固定値）
                .expiresIn(expiration / 1000)      // 有効期限を秒単位に変換
                .userId(user.getId())              // ユーザーID
                .username(user.getUsername())      // ログインユーザー名
                .displayName(user.getDisplayName()) // 表示名
                .role(user.getRole())              // ロール
                .build();
    }
}
