package com.company.pcmgmt.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RentalResponse {
    private Long id;
    private Long pcAssetId;
    private String assetNumber;
    private String deviceName;
    private Long rentalVendorId;
    private String vendorName;
    private String contractNumber;
    private LocalDate rentalStartDate;
    private LocalDate rentalEndDate;
    private BigDecimal monthlyFee;
    private String contractFilePath;
    private LocalDate returnDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 返却済みかどうか */
    public boolean isReturned() {
        return returnDate != null;
    }

    /** 期限切れかどうか（未返却） */
    public boolean isExpired() {
        if (isReturned()) return false;
        return rentalEndDate != null && rentalEndDate.isBefore(LocalDate.now());
    }

    /** 残り日数（未返却の場合、負=期限切れ） */
    public Long getDaysUntilExpiry() {
        if (isReturned() || rentalEndDate == null) return null;
        return ChronoUnit.DAYS.between(LocalDate.now(), rentalEndDate);
    }
}
