package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class LoanReturnRequest {

    @NotNull(message = "返却日は必須です")
    private LocalDate actualReturnDate;

    private String note;
}
