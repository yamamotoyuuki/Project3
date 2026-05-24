package com.company.pcmgmt.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {
    private long totalPcCount;
    private long inUsePcCount;
    private long inStoragePcCount;
    private long activeLoansCount;
    private long nearExpiryRentalsCount;   // 90日以内に期限切れ
    private long expiredRentalsCount;      // 期限切れ済み
    private long licenseOverCount;
}
