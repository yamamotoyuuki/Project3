package com.company.pcmgmt.api.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * ページネーションレスポンス DTO
 *
 * <p>一覧取得 API でページネーション情報と共にデータを返すための汎用ラッパー。
 * フロントエンドはこのレスポンスの {@code totalPages} / {@code totalElements} を用いて
 * ページネーション UI を構築する。</p>
 *
 * @param <T> ページに含まれる要素の型
 */
@Data
@Builder
public class PageResponse<T> {

    /** 現在ページのデータリスト */
    private List<T> content;

    /** 全件数（ページングなしの総レコード数） */
    private long totalElements;

    /** 総ページ数（totalElements / size の切り上げ） */
    private int totalPages;

    /** 現在のページ番号（0始まり） */
    private int page;

    /** 1ページあたりの取得件数 */
    private int size;

    /**
     * ページレスポンスを生成するファクトリメソッド
     *
     * @param content 現在ページのデータリスト
     * @param total   全件数
     * @param page    現在ページ番号（0始まり）
     * @param size    1ページあたりの件数
     * @param <T>     要素の型
     * @return 組み立て済み PageResponse
     */
    public static <T> PageResponse<T> of(List<T> content, long total, int page, int size) {
        // size が 0 の場合は 0 ページとして扱う（ゼロ除算防止）
        int totalPages = size == 0 ? 0 : (int) Math.ceil((double) total / size);
        return PageResponse.<T>builder()
                .content(content)
                .totalElements(total)
                .totalPages(totalPages)
                .page(page)
                .size(size)
                .build();
    }
}
