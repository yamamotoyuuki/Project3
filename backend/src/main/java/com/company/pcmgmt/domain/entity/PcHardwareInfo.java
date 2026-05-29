package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PC ハードウェア情報エンティティ
 *
 * <p>Tauri エージェントが収集したPC端末のハードウェア仕様を保持する。
 * 1台のPC資産に対して1レコードが存在し、エージェント報告のたびに上書き更新される（upsert）。</p>
 *
 * <p>対応テーブル: {@code pc_hardware_info}</p>
 */
@Data
public class PcHardwareInfo {

    /** ハードウェア情報ID（主キー、自動採番） */
    private Long id;

    /**
     * エージェント番号（agents テーブルとの紐付けキー）
     * <p>V4 マイグレーションにて pc_asset_id から変更。
     * エージェントが収集したハードウェア情報はエージェント番号で識別する。</p>
     */
    private String agentNumber;

    /** CPU モデル名（例: "Intel Core i7-1365U"） */
    private String cpuModel;

    /** CPU 物理コア数 */
    private Integer cpuCores;

    /** 搭載メモリ容量（GB単位、小数点あり） */
    private BigDecimal memoryGb;

    /** ディスク総容量（GB単位、小数点あり） */
    private BigDecimal diskGb;

    /** ディスク空き容量（GB単位、小数点あり） */
    private BigDecimal diskFreeGb;

    /** エージェントが情報を収集した日時 */
    private LocalDateTime collectedAt;

    /** レコード作成日時（DB自動設定） */
    private LocalDateTime createdAt;

    /** レコード更新日時（DB自動設定） */
    private LocalDateTime updatedAt;
}
