package com.company.pcmgmt.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT（JSON Web Token）トークンの生成・検証コンポーネント
 *
 * <p>ログイン時にJWTを発行し、各リクエストのトークン検証を行う。
 * HMAC-SHA256 アルゴリズムを使用して署名する。</p>
 */
@Slf4j
@Component
public class JwtTokenProvider {

    /** JWT署名用シークレットキー（application.yml の jwt.secret から注入） */
    @Value("${jwt.secret}")
    private String secret;

    /** トークン有効期限（ミリ秒、application.yml の jwt.expiration から注入） */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * HMAC-SHA256 署名用のシークレットキーを生成する
     *
     * <p>シークレットは UTF-8 でバイト変換し、32バイト未満の場合は例外をスローする。</p>
     *
     * @return HMAC-SHA256 用の SecretKey
     * @throws IllegalStateException シークレットが32バイト未満の場合
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        // HMAC-SHA256 requires at least 32 bytes
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes");
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWTトークンを生成する
     *
     * <p>ユーザー名（subject）、ユーザーID、ロールをクレームに含めて署名済みトークンを発行する。</p>
     *
     * @param userDetails Spring Security のユーザー詳細情報（ユーザー名取得用）
     * @param userId      ユーザーID（DBのPK）
     * @param role        ユーザーロール（ADMIN / IT_STAFF / VIEWER）
     * @return 署名済み JWT 文字列
     */
    public String generateToken(UserDetails userDetails, Long userId, String role) {
        // トークン発行日時
        Date now = new Date();
        // トークン有効期限日時（現在時刻 + 設定値）
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(userDetails.getUsername())   // ユーザー名をサブジェクトに設定
                .claim("userId", userId)               // カスタムクレーム: ユーザーID
                .claim("role", role)                   // カスタムクレーム: ロール
                .issuedAt(now)                         // 発行日時
                .expiration(expiryDate)                // 有効期限
                .signWith(getSigningKey())             // HMAC-SHA256 で署名
                .compact();
    }

    /**
     * トークンからユーザー名（subject）を取得する
     *
     * @param token 検証対象の JWT 文字列
     * @return トークンに含まれるユーザー名
     */
    public String getUsernameFromToken(String token) {
        // クレームを解析してサブジェクト（ユーザー名）を返す
        return parseClaims(token).getSubject();
    }

    /**
     * トークンの有効性を検証する
     *
     * <p>署名・有効期限・フォーマットを検証し、問題がなければ true を返す。
     * 不正なトークンは警告ログを出力して false を返す（例外は外部に伝播しない）。</p>
     *
     * @param token 検証対象の JWT 文字列
     * @return 有効な場合は true、無効な場合は false
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            // トークンの有効期限切れ
            log.warn("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            // サポートされていないトークン形式
            log.warn("JWT token is unsupported: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            // 不正な形式のトークン
            log.warn("JWT token is malformed: {}", e.getMessage());
        } catch (SecurityException e) {
            // 署名が無効
            log.warn("JWT signature is invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            // クレームが空
            log.warn("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * JWT トークンを解析してクレームを取得する（内部処理用）
     *
     * @param token JWT 文字列
     * @return 解析済みクレームオブジェクト
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // 署名検証キーを設定
                .build()
                .parseSignedClaims(token)      // トークンを解析
                .getPayload();                 // クレームペイロードを取得
    }
}
