package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RentalCreateRequest {

    @NotNull(message = "PC資産IDは必須です")
    private Long pcAssetId;

    @NotNull(message = "レンタルベンダーIDは必須です")
    private Long rentalVendorId;

    private String contractNumber;

    @NotNull(message = "レンタル開始日は必須です")
    private LocalDate rentalStartDate;

    @NotNull(message = "レンタル終了日は必須です")
    private LocalDate rentalEndDate;

    private BigDecimal monthlyFee;
    private String contractFilePath;
}
