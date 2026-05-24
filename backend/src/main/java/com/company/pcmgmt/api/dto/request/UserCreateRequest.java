package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UserCreateRequest {

    @NotBlank(message = "ユーザー名は必須です")
    private String username;

    @NotBlank(message = "パスワードは必須です")
    private String password;

    @NotBlank(message = "表示名は必須です")
    private String displayName;

    @NotBlank(message = "ロールは必須です")
    @Pattern(regexp = "ADMIN|IT_STAFF|VIEWER", message = "ロールは ADMIN / IT_STAFF / VIEWER を指定してください")
    private String role;

    private String email;
}
