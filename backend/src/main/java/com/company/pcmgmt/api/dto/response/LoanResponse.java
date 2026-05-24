package com.company.pcmgmt.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoanResponse {
    private Long id;
    private Long pcAssetId;
    private String assetNumber;
    private String deviceName;
    private Long employeeId;
    private String employeeName;
    private String employeeCode;
    private LocalDate loanDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private String purpose;
    private String note;
    private Long createdBy;
    private String createdByName;
    private LocalDateTime createdAt;

    /** 返却済みかどうか */
    public boolean isReturned() {
        return actualReturnDate != null;
    }

    /** 返却期限超過かどうか（未返却の場合） */
    public boolean isOverdue() {
        if (isReturned()) return false;
        if (expectedReturnDate == null) return false;
        return expectedReturnDate.isBefore(LocalDate.now());
    }
}
