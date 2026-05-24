package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class RentalVendor {
    private Long id;
    private String companyName;
    private String contactName;
    private String phone;
    private String email;
    private String address;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
