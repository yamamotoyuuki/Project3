package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * PC資産更新リクエスト DTO
 *
 * <p>PUT /api/v1/assets/{id} のリクエストボディにマッピングされる。
 * 資産番号は変更不可のため含まない。ステータス変更も本 DTO で行う。</p>
 */
@Data
public class AssetUpdateRequest {

    /** 端末名・機種の通称（必須） */
    @NotBlank(message = "端末名は必須です")
    private String deviceName;

    /**
     * 取得区分（必須）
     * PURCHASE（購入）または RENTAL（レンタル）のみ許可
     */
    @NotBlank(message = "取得区分は必須です")
    @Pattern(regexp = "PURCHASE|RENTAL", message = "取得区分は PURCHASE または RENTAL を指定してください")
    private String acquisitionType;

    /** メーカー名（任意） */
    private String maker;

    /** 型番（任意） */
    private String modelNumber;

    /** シリアル番号（任意） */
    private String serialNumber;

    /** 設置場所・保管場所（任意） */
    private String location;

    /**
     * 資産ステータス（必須）
     * IN_USE / IN_STORAGE / DISPOSED / IN_REPAIR / RETURNED のいずれか
     */
    @NotBlank(message = "ステータスは必須です")
    @Pattern(regexp = "IN_USE|IN_STORAGE|DISPOSED|IN_REPAIR|RETURNED",
             message = "ステータスが不正です")
    private String status;

    /** 割り当て社員のID（null を指定すると割り当て解除） */
    private Long assignedEmployeeId;

    /** ホスト名（エージェント紐付け用） */
    private String hostname;

    /** 備考・メモ（任意） */
    private String note;
}
