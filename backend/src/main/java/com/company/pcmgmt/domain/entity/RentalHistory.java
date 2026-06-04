package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * レンタル契約変更履歴エンティティ
 *
 * <p>レンタル契約の CREATE / UPDATE / RETURN 操作を記録する。
 * 1操作で複数フィールドが変わった場合は、同一 {@code operationId} を持つ複数行が登録される。</p>
 *
 * <p>対応テーブル: {@code pc_rental_history}</p>
 */
@Data
public class RentalHistory {

    /** レコードID（主キー、自動採番） */
    private Long id;

    /** 対象レンタル契約ID（pc_acquisition_rental.id の外部キー） */
    private Long rentalId;

    /**
     * 同一操作をまとめるUUID。
     * 1回の保存操作（create / update / return）の開始時に生成し、
     * その操作で挿入される全レコードに同じ値を付与する。
     */
    private String operationId;

    /**
     * 操作種別。
     * CREATE（契約登録）/ UPDATE（契約更新）/ RETURN（返却登録）のいずれか。
     */
    private String operation;

    /** 変更フィールド名（UPDATE 時のみ設定。例: "rentalEndDate"） */
    private String fieldName;

    /** 画面表示用ラベル（UPDATE 時のみ設定。例: "契約終了日"） */
    private String fieldLabel;

    /** 変更前の値（文字列表現。CREATE / 初期値なしの場合は null） */
    private String oldValue;

    /** 変更後の値（文字列表現） */
    private String newValue;

    /** 操作者名（users.display_name のキャッシュ。ユーザー削除後も表示できるよう保持） */
    private String changedByName;

    /** 変更日時（DB自動設定） */
    private LocalDateTime changedAt;
}
