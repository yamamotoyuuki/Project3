package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * PC取得（レンタル契約）エンティティ
 *
 * <p>PC資産のレンタル（リース）契約情報を保持する。
 * {@code returnDate} が null の場合はレンタル中、値がある場合は返却済みを示す。
 * レンタル終了日（{@code rentalEndDate}）を超えた場合は期限切れとして扱う。</p>
 *
 * <p>対応テーブル: {@code pc_acquisition_rentals}</p>
 */
@Data
public class PcAcquisitionRental {

    /** レンタル契約ID（主キー、自動採番） */
    private Long id;

    /** 対象PC資産のID（pc_assets テーブルの外部キー） */
    private Long pcAssetId;

    /** レンタルベンダーのID（rental_vendors テーブルの外部キー） */
    private Long rentalVendorId;

    /** 契約番号（ベンダーから発行された契約識別番号） */
    private String contractNumber;

    /** レンタル開始日 */
    private LocalDate rentalStartDate;

    /** レンタル終了日（契約満了日） */
    private LocalDate rentalEndDate;

    /** 月額レンタル料金（税抜き、任意） */
    private BigDecimal monthlyFee;

    /** 契約書ファイルのパス（任意。例: "contracts/rental_001.pdf"） */
    private String contractFilePath;

    /** 返却日（null の場合はレンタル中） */
    private LocalDate returnDate;

    /** 返却処理を行ったユーザーのID（users テーブルの外部キー） */
    private Long returnedBy;

    /** レコード作成日時（DB自動設定） */
    private LocalDateTime createdAt;

    /** レコード更新日時（DB自動設定） */
    private LocalDateTime updatedAt;
}
