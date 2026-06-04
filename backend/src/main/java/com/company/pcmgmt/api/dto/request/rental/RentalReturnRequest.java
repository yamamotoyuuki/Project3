package com.company.pcmgmt.api.dto.request.rental;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * レンタル返却リクエスト DTO
 *
 * <p>PUT /api/v1/rentals/{id}/return のリクエストボディにマッピングされる。
 * 画面で入力された返却日を受け取り、契約の return_date に保存する。</p>
 */
@Data
public class RentalReturnRequest {

    /** 返却日（必須）。画面で入力された実際の返却日を指定する。 */
    @NotNull(message = "返却日は必須です")
    private LocalDate returnDate;
}
