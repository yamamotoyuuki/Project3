package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * システムユーザー新規登録リクエスト DTO
 *
 * <p>POST /api/v1/users のリクエストボディにマッピングされる。
 * 管理者（ADMIN ロール）のみアクセス可能なエンドポイント。
 * パスワードはサービス層で BCrypt ハッシュ化される。</p>
 */
@Data
public class UserCreateRequest {

    /** ログインユーザー名（必須、ユニーク） */
    @NotBlank(message = "ユーザー名は必須です")
    private String username;

    /** ログインパスワード（必須、平文で受け取りサービス層でハッシュ化） */
    @NotBlank(message = "パスワードは必須です")
    private String password;

    /** 画面表示用ユーザー名（必須。例: "山田 太郎"） */
    @NotBlank(message = "表示名は必須です")
    private String displayName;

    /**
     * ユーザーロール（必須）
     * ADMIN（管理者）/ IT_STAFF（IT担当者）/ VIEWER（閲覧者）のいずれか
     */
    @NotBlank(message = "ロールは必須です")
    @Pattern(regexp = "ADMIN|IT_STAFF|VIEWER", message = "ロールは ADMIN / IT_STAFF / VIEWER を指定してください")
    private String role;

    /** メールアドレス（任意） */
    private String email;
}
