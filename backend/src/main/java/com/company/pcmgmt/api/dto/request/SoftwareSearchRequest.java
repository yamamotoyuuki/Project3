package com.company.pcmgmt.api.dto.request;

import lombok.Data;

@Data
public class SoftwareSearchRequest {
    private String keyword;    // ソフトウェア名 / 発行元
    private Boolean overLimit; // true=ライセンス超過のみ
    private int page = 0;
    private int size = 20;

    public int getOffset() { return page * size; }
}
