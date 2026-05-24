package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * レンタル契約登録リクエスト DTO
 *
 * <p>POST /api/v1/rentals のリクエストボディにマッピングされる。
 * PC資産とレンタルベンダーの存在確認はサービス層で行う。</p>
 */
@Data
public class RentalCreateRequest {

    /** レンタル対象のPC資産ID（必須） */
    @NotNull(message = "PC資産IDは必須です")
    private Long pcAssetId;

    /** レンタル契約先ベンダーのID（必須） */
    @NotNull(message = "レンタルベンダーIDは必須です")
    private Long rentalVendorId;

    /** 契約番号（任意。ベンダーが発行する契約識別番号） */
    private String contractNumber;

    /** レンタル開始日（必須） */
    @NotNull(message = "レンタル開始日は必須です")
    private LocalDate rentalStartDate;

    /** レンタル終了日・契約満了日（必須） */
    @NotNull(message = "レンタル終了日は必須です")
    private LocalDate rentalEndDate;

    /** 月額レンタル料金（任意、税抜き） */
    private BigDecimal monthlyFee;

    /** 契約書ファイルのパス（任意。例: "contracts/rental_001.pdf"） */
    private String contractFilePath;
}
