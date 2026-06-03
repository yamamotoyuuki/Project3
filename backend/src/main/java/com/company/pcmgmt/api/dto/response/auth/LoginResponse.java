package com.company.pcmgmt.api.dto.response.auth;

import lombok.Builder;
import lombok.Data;

/**
 * ログインレスポンス DTO
 *
 * <p>ログイン成功時に返却するJWTトークンおよびユーザー情報を保持する。
 * フロントエンドはこのレスポンスからトークンを取得し、以降のリクエストに Bearer 認証として付与する。</p>
 */
@Data
@Builder
public class LoginResponse {

    /** 発行された JWT アクセストークン */
    private String token;

    /** トークン種別（固定値: "Bearer"） */
    private String tokenType;

    /** トークン有効期限（秒単位） */
    private Long expiresIn;

    /** ログインしたユーザーの DB ID */
    private Long userId;

    /** ログインユーザー名 */
    private String username;

    /** 画面表示用ユーザー名（表示名） */
    private String displayName;

    /** ユーザーロール（ADMIN / IT_STAFF / VIEWER） */
    private String role;
}
