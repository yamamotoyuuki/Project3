package com.company.pcmgmt.api.dto.request.asset;

import lombok.Data;

/**
 * PC資産検索リクエスト DTO
 *
 * <p>GET /api/v1/assets のクエリパラメータにマッピングされる。
 * キーワード・ステータス・取得区分・設置場所で絞り込み、ページネーションを行う。</p>
 */
@Data
public class AssetSearchRequest {

    /**
     * 検索キーワード（部分一致）
     * 資産番号 / 端末名 / シリアル番号 / ホスト名 を横断検索
     */
    private String keyword;

    /**
     * ステータスで絞り込み（完全一致）
     * IN_USE（使用中）/ IN_STORAGE（保管中）/ DISPOSED（廃棄済）
     * / IN_REPAIR（修理中）/ RETURNED（返却済）
     */
    private String status;

    /**
     * 取得区分で絞り込み（完全一致）
     * PURCHASE（購入）/ RENTAL（レンタル）
     */
    private String acquisitionType;

    /**
     * 機器種別で絞り込み（完全一致）
     * code_master DEVICE_TYPE のコード値を指定する。
     * 例: LAPTOP（ノートPC）/ DESKTOP（デスクトップPC）/ DISPLAY（モニター）
     */
    private String deviceType;

    /** 設置場所で絞り込み（部分一致） */
    private String location;

    /** ページ番号（0始まり、デフォルト: 0） */
    private int page = 0;

    /** 1ページあたりの件数（デフォルト: 20） */
    private int size = 20;

    /**
     * MyBatis の SQL で LIMIT/OFFSET を使うためのオフセット値を計算して返す
     *
     * @return SQL の OFFSET 値（= page × size）
     */
    public int getOffset() {
        return page * size;
    }
}
