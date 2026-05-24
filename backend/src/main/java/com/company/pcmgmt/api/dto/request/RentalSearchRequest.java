package com.company.pcmgmt.api.dto.request;

import lombok.Data;

@Data
public class RentalSearchRequest {
    private String keyword;        // 資産番号 / 端末名 / ベンダー名 / 契約番号
    private String expiryFilter;   // "near" (90日以内) | "expired" (期限切れ) | null=全件
    private Boolean returned;      // true=返却済, false=契約中, null=全件
    private int page = 0;
    private int size = 20;

    public int getOffset() { return page * size; }
}
