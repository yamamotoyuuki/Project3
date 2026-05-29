package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * PC資産エンティティ
 *
 * <p>管理対象となるPC端末の資産情報を保持する。
 * 取得区分（購入 / レンタル）・現在ステータス・担当社員などを管理する。</p>
 *
 * <p>対応テーブル: {@code pc_assets}</p>
 */
@Data
public class PcAsset {

    /** PC資産ID（主キー、自動採番） */
    private Long id;

    /** 資産番号（管理番号、ユニーク制約あり。例: "PC-2024-001"） */
    private String assetNumber;

    /** 端末名・機種の通称（例: "開発用ThinkPad"） */
    private String deviceName;

    /**
     * 取得区分
     * <ul>
     *   <li>PURCHASE: 購入品</li>
     *   <li>RENTAL: レンタル品</li>
     * </ul>
     */
    private String acquisitionType;

    /** メーカー名（例: "Lenovo", "Dell", "Apple"） */
    private String maker;

    /** 型番（例: "ThinkPad X1 Carbon Gen 11"） */
    private String modelNumber;

    /** シリアル番号（製品固有の識別子） */
    private String serialNumber;

    /** 設置場所・保管場所（例: "東京本社3F", "倉庫A"） */
    private String location;

    /**
     * 資産ステータス
     * <ul>
     *   <li>IN_USE: 使用中</li>
     *   <li>IN_STORAGE: 保管中</li>
     *   <li>DISPOSED: 廃棄済み</li>
     *   <li>IN_REPAIR: 修理中</li>
     *   <li>RETURNED: 返却済み（レンタル品）</li>
     * </ul>
     */
    private String status;

    /** 現在割り当てられている社員の ID（employees テーブルの外部キー） */
    private Long assignedEmployeeId;

    /**
     * エージェントが報告した使用者名（自由テキスト）
     * <p>agents からの報告時に直接保存される。
     * {@code assignedEmployeeId} とは独立して管理し、
     * 社員マスタに一致しない場合でもエージェント入力値を表示できるようにする。</p>
     */
    private String userName;

    /**
     * エージェント番号（agents テーブルとの紐付けキー）
     * <p>エージェントアプリが初回登録時に取得した識別子（例: "AGT-A1B2C3D4"）。
     * エージェント未導入の場合は null。</p>
     */
    private String agentNumber;

    /** エージェントが認識しているホスト名（ネットワーク識別名） */
    private String hostname;

    /** エージェントから最後に情報を受信した日時（エージェント未導入の場合は null） */
    private LocalDateTime agentLastSeen;

    /** 備考・メモ（任意入力） */
    private String note;

    /** レコード作成日時（DB自動設定） */
    private LocalDateTime createdAt;

    /** レコード更新日時（DB自動設定） */
    private LocalDateTime updatedAt;

    // ---- JOIN用フィールド（DBカラムではない） ----

    /** 担当社員のフルネーム（employees テーブルとのJOINで取得、DBカラムなし） */
    private String assignedEmployeeName;
}
