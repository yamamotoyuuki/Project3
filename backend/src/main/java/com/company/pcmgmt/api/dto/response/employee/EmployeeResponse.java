package com.company.pcmgmt.api.dto.response.employee;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 社員レスポンス DTO
 *
 * <p>社員一覧・詳細 API が返却するデータ構造。
 * パスワード等のセキュリティ情報は含まない。
 * {@code null} フィールドは JSON 出力から除外される（{@code @JsonInclude(NON_NULL)}）。</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EmployeeResponse {

    /** 社員ID */
    private Long id;

    /** 社員コード（管理番号。例: "EMP-0001"） */
    private String employeeCode;

    /** 社員のフルネーム（氏名） */
    private String fullName;

    /** 所属部署名 */
    private String department;

    /** 役職名 */
    private String position;

    /** メールアドレス */
    private String email;

    /** 電話番号 */
    private String phone;

    /** 勤務場所・拠点 */
    private String location;

    /** 在籍フラグ（true: 在籍中, false: 退職済み） */
    private Boolean isActive;

    /** レコード作成日時 */
    private LocalDateTime createdAt;

    /** レコード更新日時 */
    private LocalDateTime updatedAt;
}
