package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PcAsset {
    private Long id;
    private String assetNumber;
    private String deviceName;
    private String acquisitionType;   // PURCHASE | RENTAL
    private String maker;
    private String modelNumber;
    private String serialNumber;
    private String location;
    private String status;            // IN_USE | IN_STORAGE | DISPOSED | IN_REPAIR | RETURNED
    private Long assignedEmployeeId;
    private String hostname;
    private LocalDateTime agentLastSeen;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // JOIN用フィールド（DBカラムではない）
    private String assignedEmployeeName;
}
