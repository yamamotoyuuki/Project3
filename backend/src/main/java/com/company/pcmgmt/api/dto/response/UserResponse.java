package com.company.pcmgmt.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * システムユーザーレスポンス DTO
 *
 * <p>ユーザー一覧・詳細 API が返却するデータ構造。
 * パスワードハッシュ等のセキュリティ情報は含まない。
 * {@code null} フィールドは JSON 出力から除外される（{@code @JsonInclude(NON_NULL)}）。</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    /** ユーザーID */
    private Long id;

    /** ログインユーザー名 */
    private String username;

    /** 画面表示用ユーザー名（表示名） */
    private String displayName;

    /** ユーザーロール（ADMIN / IT_STAFF / VIEWER） */
    private String role;

    /** メールアドレス */
    private String email;

    /** アカウント有効フラグ（true: 有効, false: 無効） */
    private Boolean isActive;

    /** 最終ログイン日時 */
    private LocalDateTime lastLoginAt;

    /** レコード作成日時 */
    private LocalDateTime createdAt;

    /** レコード更新日時 */
    private LocalDateTime updatedAt;
}
