package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * エージェントエンティティ
 *
 * <p>Tauri エージェントアプリがインストールされた端末を識別するマスタ情報。
 * {@code agent_number} を PRIMARY KEY として {@code pc_assets} / {@code agent_history} と紐づける。</p>
 *
 * <p>対応テーブル: {@code agents}</p>
 */
@Data
public class Agent {

    /**
     * エージェント番号（主キー）
     * <p>バックエンドが生成する端末固有の識別子。例: "AGT-A1B2C3D4"</p>
     */
    private String agentNumber;

    /** エージェントが動作するPCのホスト名 */
    private String hostname;

    /** 初回登録日時 */
    private LocalDateTime createdAt;

    /** 最終更新日時 */
    private LocalDateTime updatedAt;
}
