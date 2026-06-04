package com.company.pcmgmt.api.dto.response.rental;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * レンタル契約変更履歴レスポンス DTO
 *
 * <p>GET /api/v1/rentals/{id}/histories のレスポンスアイテム型。
 * フロントエンドでは {@code operationId} をキーにグルーピングし、
 * 1操作単位で変更内容を表示する。</p>
 */
@Data
public class RentalHistoryResponse {

    /** レコードID */
    private Long id;

    /** 対象レンタル契約ID */
    private Long rentalId;

    /** 同一操作をまとめる UUID（グルーピングキー） */
    private String operationId;

    /** 操作種別（CREATE / UPDATE / RETURN） */
    private String operation;

    /** 変更フィールド名（UPDATE 時のみ。例: "rentalEndDate"） */
    private String fieldName;

    /** 画面表示用ラベル（UPDATE 時のみ。例: "契約終了日"） */
    private String fieldLabel;

    /** 変更前の値（null = 未設定または新規登録） */
    private String oldValue;

    /** 変更後の値 */
    private String newValue;

    /** 操作者名 */
    private String changedByName;

    /** 変更日時（ISO 8601） */
    private LocalDateTime changedAt;
}
