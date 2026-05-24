package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SoftwareCreateRequest {

    @NotBlank(message = "ソフトウェア名は必須です")
    private String softwareName;

    private String publisher;
    private String licenseType;

    @Min(value = 0, message = "購入ライセンス数は0以上を指定してください")
    private Integer purchasedCount = 0;

    private String note;
}
