package com.company.pcmgmt.api.dto.response.agent;

import lombok.Data;

/**
 * エージェント起動時の資産情報レスポンス DTO
 *
 * <p>GET /api/v1/agent/asset-info が返却するデータ構造。
 * 取得区分に加え、レンタルの場合は返却済みかどうかも返す。</p>
 *
 * <p>フィールド:
 * <ul>
 *   <li>acquisitionType: "PURCHASE" / "RENTAL" / null（未登録・UNKNOWN）</li>
 *   <li>returned       : true = レンタル返却済み / false = 未返却または購入品</li>
 * </ul>
 * </p>
 */
@Data
public class AssetInfoResponse {

    /**
     * 取得区分
     * "PURCHASE"（購入）/ "RENTAL"（レンタル）/ null（未登録・UNKNOWN）
     */
    private String acquisitionType;

    /**
     * 返却済みフラグ
     * true  : RENTAL かつ pc_acquisition_rental.return_date が設定済み（返却済み）
     * false : 購入品 / 未返却 / 資産未登録
     *
     * Lombok @Data で isReturned() getter が生成され、
     * Jackson は "returned" キーとして JSON にシリアライズする。
     */
    private boolean returned;
}
