package com.company.pcmgmt.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetResponse {
    private Long id;
    private String assetNumber;
    private String deviceName;
    private String acquisitionType;
    private String maker;
    private String modelNumber;
    private String serialNumber;
    private String location;
    private String status;
    private Long assignedEmployeeId;
    private String assignedEmployeeName;
    private String hostname;
    private LocalDateTime agentLastSeen;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
