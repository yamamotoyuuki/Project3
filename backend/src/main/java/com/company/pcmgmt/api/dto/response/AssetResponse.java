package com.company.pcmgmt.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * PC資産レスポンス DTO
 *
 * <p>PC資産一覧・詳細 API が返却するデータ構造。
 * {@code null} フィールドは JSON 出力から除外される（{@code @JsonInclude(NON_NULL)}）。</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AssetResponse {

    /** PC資産ID */
    private Long id;

    /** 資産番号（管理番号。例: "PC-2024-001"） */
    private String assetNumber;

    /** 端末名・機種の通称 */
    private String deviceName;

    /** 取得区分（PURCHASE: 購入 / RENTAL: レンタル） */
    private String acquisitionType;

    /** メーカー名 */
    private String maker;

    /** 型番 */
    private String modelNumber;

    /** シリアル番号 */
    private String serialNumber;

    /** 設置場所・保管場所 */
    private String location;

    /** 資産ステータス（IN_USE / IN_STORAGE / DISPOSED / IN_REPAIR / RETURNED） */
    private String status;

    /** 割り当て社員のID（未割り当ての場合は null） */
    private Long assignedEmployeeId;

    /** 割り当て社員のフルネーム（JOINで取得、未割り当ての場合は null） */
    private String assignedEmployeeName;

    /** ホスト名（エージェントが認識しているネットワーク名） */
    private String hostname;

    /** エージェントから最後に情報を受信した日時（エージェント未導入の場合は null） */
    private LocalDateTime agentLastSeen;

    /** 備考・メモ */
    private String note;

    /** レコード作成日時 */
    private LocalDateTime createdAt;

    /** レコード更新日時 */
    private LocalDateTime updatedAt;
}
