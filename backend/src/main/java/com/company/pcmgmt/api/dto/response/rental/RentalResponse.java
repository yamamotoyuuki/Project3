package com.company.pcmgmt.api.dto.response.rental;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * レンタル契約レスポンス DTO
 *
 * <p>レンタル契約一覧・詳細 API が返却するデータ構造。
 * PC資産情報・ベンダー情報を JOIN して取得した項目を含む。
 * ヘルパーメソッドで返却状況・期限超過・残り日数を判定できる。
 * {@code null} フィールドは JSON 出力から除外される（{@code @JsonInclude(NON_NULL)}）。</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RentalResponse {

    /** レンタル契約ID */
    private Long id;

    /** レンタル対象のPC資産ID */
    private Long pcAssetId;

    /** レンタル対象の資産番号（JOINで取得） */
    private String assetNumber;

    /** レンタル対象の端末名（JOINで取得） */
    private String deviceName;

    /** レンタルベンダーのID */
    private Long rentalVendorId;

    /** レンタルベンダーの会社名（JOINで取得） */
    private String vendorName;

    /** 契約番号（ベンダーが発行する識別番号） */
    private String contractNumber;

    /** レンタル開始日 */
    private LocalDate rentalStartDate;

    /** レンタル終了日・契約満了日 */
    private LocalDate rentalEndDate;

    /** 月額レンタル料金（税抜き） */
    private BigDecimal monthlyFee;

    /** 契約書ファイルのパス */
    private String contractFilePath;

    /** 返却日（null の場合はレンタル中） */
    private LocalDate returnDate;

    /** レコード作成日時 */
    private LocalDateTime createdAt;

    /** レコード更新日時 */
    private LocalDateTime updatedAt;

    /**
     * 返却済みかどうかを判定する
     *
     * @return 返却日（returnDate）が設定されている場合は true
     */
    public boolean isReturned() {
        return returnDate != null;
    }

    /**
     * 期限切れかどうかを判定する（未返却の場合のみ有効）
     *
     * <p>返却済みの場合は常に false を返す。
     * 未返却で、かつ終了日が今日より前の場合に true を返す。</p>
     *
     * @return 期限切れの場合は true
     */
    public boolean isExpired() {
        // 返却済みの場合は期限切れ扱いしない
        if (isReturned()) return false;
        // 終了日が設定されており、かつ今日より前の場合は期限切れ
        return rentalEndDate != null && rentalEndDate.isBefore(LocalDate.now());
    }

    /**
     * レンタル終了日までの残り日数を取得する
     *
     * <p>返却済みまたは終了日未設定の場合は null を返す。
     * 残り日数が負の場合は期限切れを意味する。</p>
     *
     * @return 残り日数（負の値は期限超過、null は計算不可）
     */
    public Long getDaysUntilExpiry() {
        // 返却済みまたは終了日未設定の場合は計算不可
        if (isReturned() || rentalEndDate == null) return null;
        // 今日からレンタル終了日までの日数を計算（負の値 = 期限超過）
        return ChronoUnit.DAYS.between(LocalDate.now(), rentalEndDate);
    }
}
