package com.company.pcmgmt.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SoftwareResponse {
    private Long id;
    private String softwareName;
    private String publisher;
    private String licenseType;
    private Integer purchasedCount;
    private Integer installedCount;   // pc_software_info からの集計
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** ライセンス超過かどうか */
    public boolean isOverLimit() {
        if (purchasedCount == null || purchasedCount == 0) return false;
        return installedCount != null && installedCount > purchasedCount;
    }
}
