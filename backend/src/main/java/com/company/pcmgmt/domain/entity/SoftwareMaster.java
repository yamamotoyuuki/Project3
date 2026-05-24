package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SoftwareMaster {
    private Long id;
    private String softwareName;
    private String publisher;
    private String licenseType;
    private Integer purchasedCount;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
