package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * ソフトウェアマスタエンティティ
 *
 * <p>管理対象ソフトウェアのライセンス情報を保持するマスタデータ。
 * エージェントが収集したインストール情報と照合し、ライセンス超過を検出する。</p>
 *
 * <p>対応テーブル: {@code software_master}</p>
 */
@Data
public class SoftwareMaster {

    /** ソフトウェアマスタID（主キー、自動採番） */
    private Long id;

    /** ソフトウェア名（例: "Microsoft Office 2021", "Adobe Acrobat"） */
    private String softwareName;

    /** 発行元・メーカー名（例: "Microsoft", "Adobe"） */
    private String publisher;

    /**
     * ライセンス種別（任意。例: "シートライセンス", "デバイスライセンス", "サイトライセンス"）
     */
    private String licenseType;

    /** 購入ライセンス数（0の場合はライセンス数管理なし） */
    private Integer purchasedCount;

    /** 備考・メモ（任意） */
    private String note;

    /** レコード作成日時（DB自動設定） */
    private LocalDateTime createdAt;

    /** レコード更新日時（DB自動設定） */
    private LocalDateTime updatedAt;
}
