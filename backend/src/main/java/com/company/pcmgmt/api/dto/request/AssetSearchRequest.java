package com.company.pcmgmt.api.dto.request;

import lombok.Data;

@Data
public class AssetSearchRequest {
    private String keyword;           // 資産番号 / 端末名 / シリアル / ホスト名
    private String status;            // IN_USE | IN_STORAGE | DISPOSED | IN_REPAIR | RETURNED
    private String acquisitionType;   // PURCHASE | RENTAL
    private String location;
    private int page = 0;
    private int size = 20;

    public int getOffset() {
        return page * size;
    }
}
