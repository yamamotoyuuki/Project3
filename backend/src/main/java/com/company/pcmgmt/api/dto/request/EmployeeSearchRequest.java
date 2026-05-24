package com.company.pcmgmt.api.dto.request;

import lombok.Data;

@Data
public class EmployeeSearchRequest {
    private String keyword;     // 社員コード / 氏名 / 部署
    private Boolean isActive;   // null = 全件、true = 在籍、false = 退職
    private int page = 0;
    private int size = 20;

    public int getOffset() {
        return page * size;
    }
}
