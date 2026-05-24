package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RentalVendorCreateRequest {

    @NotBlank(message = "会社名は必須です")
    private String companyName;

    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String note;
}
