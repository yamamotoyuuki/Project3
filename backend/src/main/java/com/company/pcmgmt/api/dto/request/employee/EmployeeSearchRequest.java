package com.company.pcmgmt.api.dto.request.employee;

import lombok.Data;

/**
 * 社員検索リクエスト DTO
 *
 * <p>GET /api/v1/employees のクエリパラメータにマッピングされる。
 * キーワードと在籍フラグで絞り込み、ページネーションを行う。</p>
 */
@Data
public class EmployeeSearchRequest {

    /**
     * 検索キーワード（部分一致）
     * 社員コード / 氏名 / 部署名 を横断検索
     */
    private String keyword;

    /**
     * 在籍フラグで絞り込み
     * null: 全件、true: 在籍中のみ、false: 退職済みのみ
     */
    private Boolean isActive;

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
