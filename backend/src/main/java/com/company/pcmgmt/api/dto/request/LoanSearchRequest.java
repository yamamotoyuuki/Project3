package com.company.pcmgmt.api.dto.request;

import lombok.Data;

@Data
public class LoanSearchRequest {
    private String keyword;      // 資産番号 / 端末名 / 社員名
    private Boolean returned;    // null=全件, false=貸出中, true=返却済
    private int page = 0;
    private int size = 20;

    public int getOffset() { return page * size; }
}
