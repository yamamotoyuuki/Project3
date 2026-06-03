package com.company.pcmgmt.api.dto.request.software;

import lombok.Data;

/**
 * ソフトウェア検索リクエスト DTO
 *
 * <p>GET /api/v1/software のクエリパラメータにマッピングされる。
 * キーワードとライセンス超過フラグで絞り込み、ページネーションを行う。</p>
 */
@Data
public class SoftwareSearchRequest {

    /**
     * 検索キーワード（部分一致）
     * ソフトウェア名 / 発行元名 を横断検索
     */
    private String keyword;

    /**
     * ライセンス超過フィルター
     * true: ライセンス超過（インストール数 > 購入数）のソフトウェアのみ表示
     * null または false: 全件表示
     */
    private Boolean overLimit;

    /** ページ番号（0始まり、デフォルト: 0） */
    private int page = 0;

    /** 1ページあたりの件数（デフォルト: 20） */
    private int size = 20;

    /**
     * MyBatis の SQL で LIMIT/OFFSET を使うためのオフセット値を計算して返す
     *
     * @return SQL の OFFSET 値（= page × size）
     */
    public int getOffset() { return page * size; }
}
