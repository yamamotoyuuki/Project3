package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EmployeeCreateRequest {

    @NotBlank(message = "社員コードは必須です")
    private String employeeCode;

    @NotBlank(message = "氏名は必須です")
    private String fullName;

    private String department;
    private String position;
    private String email;
    private String phone;
    private String location;
}
