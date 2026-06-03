package com.company.pcmgmt.api.dto.response.loan;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PC貸出レスポンス DTO
 *
 * <p>貸出一覧・詳細 API が返却するデータ構造。
 * PC資産情報・社員情報を JOIN して取得した項目を含む。
 * {@code null} フィールドは JSON 出力から除外される（{@code @JsonInclude(NON_NULL)}）。</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanResponse {

    /** 貸出記録ID */
    private Long id;

    /** 貸し出したPC資産のID */
    private Long pcAssetId;

    /** 貸し出したPC資産の資産番号（JOINで取得） */
    private String assetNumber;

    /** 貸し出したPC資産の端末名（JOINで取得） */
    private String deviceName;

    /** 貸出先社員のID */
    private Long employeeId;

    /** 貸出先社員のフルネーム（JOINで取得） */
    private String employeeName;

    /** 貸出先社員の社員コード（JOINで取得） */
    private String employeeCode;

    /** 貸出日 */
    private LocalDate loanDate;

    /** 返却予定日（任意、null の場合は期限なし） */
    private LocalDate expectedReturnDate;

    /** 実際の返却日（null の場合は貸出中） */
    private LocalDate actualReturnDate;

    /** 貸出目的・用途 */
    private String purpose;

    /** 備考・メモ */
    private String note;

    /** 貸出登録を行ったユーザーのID */
    private Long createdBy;

    /** 貸出登録を行ったユーザーの表示名（JOINで取得） */
    private String createdByName;

    /** レコード作成日時 */
    private LocalDateTime createdAt;

    /**
     * 返却済みかどうかを判定する
     *
     * @return 実際の返却日（actualReturnDate）が設定されている場合は true
     */
    public boolean isReturned() {
        return actualReturnDate != null;
    }

    /**
     * 返却期限を超過しているかどうかを判定する
     *
     * <p>返却済みの場合や返却予定日が未設定の場合は false を返す。
     * 未返却で返却予定日が今日より前の場合に true を返す。</p>
     *
     * @return 期限超過の場合は true
     */
    public boolean isOverdue() {
        // 返却済みの場合は超過なし
        if (isReturned()) return false;
        // 返却予定日が未設定の場合は超過なし
        if (expectedReturnDate == null) return false;
        // 返却予定日が今日より前の場合は超過
        return expectedReturnDate.isBefore(LocalDate.now());
    }
}
