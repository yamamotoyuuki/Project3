package com.company.pcmgmt.api.dto.request.rental;

import lombok.Data;

/**
 * レンタル契約検索リクエスト DTO
 *
 * <p>GET /api/v1/rentals のクエリパラメータにマッピングされる。
 * キーワード・期限フィルター・返却状況で絞り込み、ページネーションを行う。</p>
 */
@Data
public class RentalSearchRequest {

    /**
     * 検索キーワード（部分一致）
     * 資産番号 / 端末名 / ベンダー名 / 契約番号 を横断検索
     */
    private String keyword;

    /**
     * 期限フィルター
     * "near": 90日以内に終了する契約のみ
     * "expired": 既に終了日を過ぎた契約のみ
     * null: 全件
     */
    private String expiryFilter;

    /**
     * 返却状況で絞り込み
     * true: 返却済みのみ / false: レンタル中のみ / null: 全件
     */
    private Boolean returned;

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
