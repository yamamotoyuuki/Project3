package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AssetCreateRequest {

    @NotBlank(message = "資産番号は必須です")
    private String assetNumber;

    @NotBlank(message = "端末名は必須です")
    private String deviceName;

    @NotBlank(message = "取得区分は必須です")
    @Pattern(regexp = "PURCHASE|RENTAL", message = "取得区分は PURCHASE または RENTAL を指定してください")
    private String acquisitionType;

    private String maker;
    private String modelNumber;
    private String serialNumber;
    private String location;
    private String status = "IN_STORAGE";
    private Long assignedEmployeeId;
    private String hostname;
    private String note;
}
