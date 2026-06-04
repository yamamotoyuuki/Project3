package com.company.pcmgmt.api.dto.request.asset;

import lombok.Data;

import java.util.Arrays;
import java.util.List;

/**
 * PC資産検索リクエスト DTO
 *
 * <p>GET /api/v1/assets のクエリパラメータにマッピングされる。
 * キーワード・ステータス・取得区分・機器種別・設置場所で絞り込み、
 * ページネーションを行う。</p>
 *
 * <p>ステータス・取得区分・機器種別はカンマ区切りの複数値指定に対応する。
 * チェックした値を「含む」条件として IN 検索を行う。</p>
 */
@Data
public class AssetSearchRequest {

    /**
     * 検索キーワード（部分一致）
     * 資産番号 / 端末名 / シリアル番号 / ホスト名 を横断検索
     */
    private String keyword;

    /**
     * ステータスで絞り込み（カンマ区切り複数指定可）
     * 例: "IN_USE,IN_STORAGE"
     */
    private String statuses;

    /**
     * 取得区分で絞り込み（カンマ区切り複数指定可）
     * 例: "PURCHASE,RENTAL"
     */
    private String acquisitionTypes;

    /**
     * 機器種別で絞り込み（カンマ区切り複数指定可）
     * code_master DEVICE_TYPE のコード値を指定する。
     * 例: "LAPTOP,DESKTOP"
     */
    private String deviceTypes;

    /** 設置場所で絞り込み（部分一致） */
    private String location;

    /** ページ番号（0始まり、デフォルト: 0） */
    private int page = 0;

    /** 1ページあたりの件数（デフォルト: 20） */
    private int size = 20;

    /**
     * MyBatis の SQL で LIMIT/OFFSET を使うためのオフセット値を返す
     *
     * @return SQL の OFFSET 値（= page × size）
     */
    public int getOffset() {
        return page * size;
    }

    /**
     * statuses をカンマ区切りで分割した List を返す。
     * MyBatis の foreach タグで使用する。
     *
     * @return ステータスコード値のリスト（未指定の場合は空リスト）
     */
    public List<String> getStatusList() {
        return parseList(statuses);
    }

    /**
     * acquisitionTypes をカンマ区切りで分割した List を返す。
     * MyBatis の foreach タグで使用する。
     *
     * @return 取得区分コード値のリスト（未指定の場合は空リスト）
     */
    public List<String> getAcquisitionTypeList() {
        return parseList(acquisitionTypes);
    }

    /**
     * deviceTypes をカンマ区切りで分割した List を返す。
     * MyBatis の foreach タグで使用する。
     *
     * @return 機器種別コード値のリスト（未指定の場合は空リスト）
     */
    public List<String> getDeviceTypeList() {
        return parseList(deviceTypes);
    }

    /**
     * カンマ区切り文字列を List&lt;String&gt; に変換する共通ヘルパー。
     * null・空文字・空白のみの場合は空リストを返す。
     *
     * @param csv カンマ区切りの文字列（例: "IN_USE,IN_STORAGE"）
     * @return 各値をトリムした文字列リスト
     */
    private List<String> parseList(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
    }
}
