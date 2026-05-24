package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ログインリクエスト DTO
 *
 * <p>POST /api/v1/auth/login のリクエストボディにマッピングされる。
 * ユーザー名とパスワードの入力値を受け取り、認証処理に渡す。</p>
 */
@Data
public class LoginRequest {

    /** ログインユーザー名（必須） */
    @NotBlank(message = "ユーザー名は必須です")
    private String username;

    /** ログインパスワード（平文、必須） */
    @NotBlank(message = "パスワードは必須です")
    private String password;
}
