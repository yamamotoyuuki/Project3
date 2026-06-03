package com.company.pcmgmt.api.dto.request.loan;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

/**
 * PC返却登録リクエスト DTO
 *
 * <p>PUT /api/v1/loans/{id}/return のリクエストボディにマッピングされる。
 * 既に返却済みの貸出に対してリクエストするとサービス層でエラーになる。</p>
 */
@Data
public class LoanReturnRequest {

    /** 実際の返却日（必須） */
    @NotNull(message = "返却日は必須です")
    private LocalDate actualReturnDate;

    /** 返却時の備考・メモ（任意。損傷状況等の記録用） */
    private String note;
}
