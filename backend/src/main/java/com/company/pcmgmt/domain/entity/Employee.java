package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * 社員エンティティ
 *
 * <p>PC資産の担当者・貸出先として紐付けられる社員情報を保持する。
 * 在籍フラグで現役社員と退職者を区別する。</p>
 *
 * <p>対応テーブル: {@code employees}</p>
 */
@Data
public class Employee {

    /** 社員ID（主キー、自動採番） */
    private Long id;

    /** 社員コード（社内管理番号、ユニーク制約あり。例: "EMP-0001"） */
    private String employeeCode;

    /** 社員のフルネーム（氏名） */
    private String fullName;

    /** 所属部署名（例: "情報システム部", "営業部"） */
    private String department;

    /** 役職名（例: "課長", "一般"） */
    private String position;

    /** 社員のメールアドレス */
    private String email;

    /** 社員の電話番号（内線番号または携帯） */
    private String phone;

    /** 勤務場所・拠点（例: "東京本社", "大阪支社"） */
    private String location;

    /** 在籍フラグ（true: 在籍中, false: 退職済み） */
    private Boolean isActive;

    /** レコード作成日時（DB自動設定） */
    private LocalDateTime createdAt;

    /** レコード更新日時（DB自動設定） */
    private LocalDateTime updatedAt;
}
