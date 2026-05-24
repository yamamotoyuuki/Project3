package com.company.pcmgmt.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

/**
 * ダッシュボード統計情報レスポンス DTO
 *
 * <p>GET /api/v1/dashboard/stats が返却するシステム全体の集計サマリ情報。
 * フロントエンドのダッシュボード画面でウィジェット表示に使用される。
 * {@code null} フィールドは JSON 出力から除外される（{@code @JsonInclude(NON_NULL)}）。</p>
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardStatsResponse {

    /** PC資産の総台数（ステータス問わず全件） */
    private long totalPcCount;

    /** ステータスが「使用中（IN_USE）」のPC台数 */
    private long inUsePcCount;

    /** ステータスが「保管中（IN_STORAGE）」のPC台数 */
    private long inStoragePcCount;

    /** 現在貸出中（未返却）の貸出件数 */
    private long activeLoansCount;

    /** レンタル終了日まで90日以内（未返却）のレンタル契約件数 */
    private long nearExpiryRentalsCount;

    /** レンタル終了日を超過している（未返却）のレンタル契約件数 */
    private long expiredRentalsCount;

    /** ライセンス購入数を超えてインストールされているソフトウェアの件数 */
    private long licenseOverCount;
}
