package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class PcAcquisitionRental {
    private Long id;
    private Long pcAssetId;
    private Long rentalVendorId;
    private String contractNumber;
    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;
    private BigDecimal monthlyFee;
    private String contractFilePath;
    private LocalDate returnDate;
    private Long returnedBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
