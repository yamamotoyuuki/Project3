package com.company.pcmgmt.api.dto.response.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * コードマスタ値レスポンス DTO
 *
 * <p>共通コードマスタ API が返すデータ構造。
 * 各要素はドロップダウンリストの1選択肢（コード値と表示ラベルのペア）を表す。</p>
 *
 * <p>null フィールドは JSON 出力から除外される（{@code @JsonInclude(NON_NULL)}）。</p>
 *
 * <p>レスポンス例:
 * <pre>
 * {
 *   "codeValue": "IN_USE",
 *   "codeLabel": "使用中"
 * }
 * </pre>
 * </p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CodeValueResponse {

    /**
     * コード値（他テーブルのカラムに実際に格納される定数文字列）
     * 例: "IN_USE", "LAPTOP", "PURCHASE"
     */
    private String codeValue;

    /**
     * 表示ラベル（画面に表示する日本語名称）
     * 例: "使用中", "ノートPC（Laptop）", "購入"
     */
    private String codeLabel;
}
