package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LoanCreateRequest {

    @NotNull(message = "PC資産IDは必須です")
    private Long pcAssetId;

    @NotNull(message = "社員IDは必須です")
    private Long employeeId;

    @NotNull(message = "貸出日は必須です")
    private LocalDate loanDate;

    private LocalDate expectedReturnDate;
    private String purpose;
    private String note;
}
