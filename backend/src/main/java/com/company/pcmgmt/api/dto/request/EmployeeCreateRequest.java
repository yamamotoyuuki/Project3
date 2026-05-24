package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 社員新規登録リクエスト DTO
 *
 * <p>POST /api/v1/employees のリクエストボディにマッピングされる。
 * 登録直後は在籍フラグ（isActive）が true に設定される。</p>
 */
@Data
public class EmployeeCreateRequest {

    /** 社員コード（必須、ユニーク。例: "EMP-0001"） */
    @NotBlank(message = "社員コードは必須です")
    private String employeeCode;

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
}
