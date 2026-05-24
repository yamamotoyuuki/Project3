package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 社員情報更新リクエスト DTO
 *
 * <p>PUT /api/v1/employees/{id} のリクエストボディにマッピングされる。
 * 社員コードは変更不可のため含まない。退職処理は {@code isActive=false} で行う。</p>
 */
@Data
public class EmployeeUpdateRequest {

    /** 社員のフルネーム（氏名、必須） */
    @NotBlank(message = "氏名は必須です")
    private String fullName;

    /** 所属部署名（任意） */
    private String department;

    /** 役職名（任意） */
    private String position;

    /** メールアドレス（任意） */
    private String email;

    /** 電話番号（任意） */
    private String phone;

    /** 勤務場所・拠点（任意） */
    private String location;

    /**
     * 在籍フラグ（任意）
     * true: 在籍中 / false: 退職済み
     * null の場合は現在の値を維持する
     */
    private Boolean isActive;
}
