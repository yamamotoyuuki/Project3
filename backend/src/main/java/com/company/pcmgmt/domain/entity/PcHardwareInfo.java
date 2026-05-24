package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PcHardwareInfo {
    private Long id;
    private Long pcAssetId;
    private String cpuModel;
    private Integer cpuCores;
    private BigDecimal memoryGb;
    private BigDecimal diskGb;
    private BigDecimal diskFreeGb;
    private LocalDateTime collectedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
