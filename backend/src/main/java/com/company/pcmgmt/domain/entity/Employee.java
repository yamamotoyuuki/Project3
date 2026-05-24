package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Employee {
    private Long id;
    private String employeeCode;
    private String fullName;
    private String department;
    private String position;
    private String email;
    private String phone;
    private String location;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
