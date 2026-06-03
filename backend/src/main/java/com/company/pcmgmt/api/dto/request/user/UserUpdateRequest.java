package com.company.pcmgmt.api.dto.request.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * システムユーザー更新リクエスト DTO
 *
 * <p>PUT /api/v1/users/{id} のリクエストボディにマッピングされる。
 * 管理者（ADMIN ロール）のみアクセス可能なエンドポイント。
 * ユーザー名は変更不可のため含まない。</p>
 */
@Data
public class UserUpdateRequest {

    /** 画面表示用ユーザー名（必須） */
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

    /**
     * アカウント有効フラグ（任意）
     * true: 有効 / false: 無効（アカウント停止）
     * null の場合は現在の値を維持する
     */
    private Boolean isActive;

    /**
     * 新しいパスワード（任意）
     * null または空文字の場合はパスワードを変更しない
     */
    private String password;
}
