package com.company.pcmgmt.api.dto.request.loan;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * PC貸出登録リクエスト DTO
 *
 * <p>POST /api/v1/loans のリクエストボディにマッピングされる。
 * 対象PCが既に貸出中の場合はサービス層で {@code IllegalStateException} がスローされる。</p>
 */
@Data
public class LoanCreateRequest {

    /** 貸し出すPC資産のID（必須） */
    @NotNull(message = "PC資産IDは必須です")
    private Long pcAssetId;

    /** 貸出先社員のID（必須） */
    @NotNull(message = "社員IDは必須です")
    private Long employeeId;

    /** 貸出日（必須） */
    @NotNull(message = "貸出日は必須です")
    private LocalDate loanDate;

    /** 返却予定日（任意。期限なしの場合は null） */
    private LocalDate expectedReturnDate;

    /** 貸出目的・用途（任意。例: "テレワーク用", "出張用"） */
    private String purpose;

    /** 備考・メモ（任意） */
    private String note;
}
