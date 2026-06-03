package com.company.pcmgmt.api.dto.request.loan;

import lombok.Data;

/**
 * PC貸出検索リクエスト DTO
 *
 * <p>GET /api/v1/loans のクエリパラメータにマッピングされる。
 * キーワードと返却状況で絞り込み、ページネーションを行う。</p>
 */
@Data
public class LoanSearchRequest {

    /**
     * 検索キーワード（部分一致）
     * 資産番号 / 端末名 / 社員名 を横断検索
     */
    private String keyword;

    /**
     * 返却状況で絞り込み
     * null: 全件、false: 貸出中（未返却）のみ、true: 返却済みのみ
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
