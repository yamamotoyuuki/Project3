package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PcLoan {
    private Long id;
    private Long pcAssetId;
    private Long employeeId;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private String purpose;
    private String note;
    private Long createdBy;
    private LocalDateTime createdAt;
}
