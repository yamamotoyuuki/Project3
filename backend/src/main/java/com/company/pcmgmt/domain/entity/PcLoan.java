package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PC貸出エンティティ
 *
 * <p>社員へのPC端末の一時貸出記録を保持する。
 * {@code actualReturnDate} が null の場合は貸出中、値がある場合は返却済みを示す。</p>
 *
 * <p>対応テーブル: {@code pc_loans}</p>
 */
@Data
public class PcLoan {

    /** 貸出記録ID（主キー、自動採番） */
    private Long id;

    /** 貸し出すPC資産のID（pc_assets テーブルの外部キー） */
    private Long pcAssetId;

    /** 貸出先社員のID（employees テーブルの外部キー） */
    private Long employeeId;

    /** 貸出日 */
    private LocalDate loanDate;

    /** 返却予定日（任意、null の場合は期限なし） */
    private LocalDate expectedReturnDate;

    /** 実際の返却日（null の場合は貸出中） */
    private LocalDate actualReturnDate;

    /** 貸出目的・用途（任意。例: "テレワーク用", "出張用"） */
    private String purpose;

    /** 備考・メモ（任意） */
    private String note;

    /** 貸出登録を行ったユーザーのID（users テーブルの外部キー） */
    private Long createdBy;

    /** レコード作成日時（DB自動設定） */
    private LocalDateTime createdAt;
}
