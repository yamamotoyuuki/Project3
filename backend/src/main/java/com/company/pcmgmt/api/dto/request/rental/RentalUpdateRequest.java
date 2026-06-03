package com.company.pcmgmt.api.dto.request.rental;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * レンタル契約更新リクエスト DTO
 *
 * <p>PUT /api/v1/rentals/{id} のリクエストボディにマッピングされる。
 * PC 資産の変更は不可のため pcAssetId は含めない。
 * 返却日 (returnDate) は PUT /api/v1/rentals/{id}/return で別途更新する。</p>
 *
 * <p>更新可能フィールド:
 * <ul>
 *   <li>rentalVendorId  : レンタルベンダー（必須）</li>
 *   <li>contractNumber  : 契約番号（任意）</li>
 *   <li>rentalStartDate : レンタル開始日（必須）</li>
 *   <li>rentalEndDate   : レンタル終了日（必須）</li>
 *   <li>monthlyFee      : 月額料金（任意）</li>
 * </ul>
 * </p>
 */
@Data
public class RentalUpdateRequest {

    /** レンタルベンダー ID（必須） */
    @NotNull(message = "ベンダーは必須です")
    private Long rentalVendorId;

    /** 契約番号（任意。ベンダーが発行する契約識別番号） */
    private String contractNumber;

    /** レンタル開始日（必須） */
    @NotNull(message = "開始日は必須です")
    private LocalDate rentalStartDate;

    /** レンタル終了日・契約満了日（必須） */
    @NotNull(message = "終了日は必須です")
    private LocalDate rentalEndDate;

    /** 月額レンタル料金（任意、税抜き） */
    private BigDecimal monthlyFee;
}
