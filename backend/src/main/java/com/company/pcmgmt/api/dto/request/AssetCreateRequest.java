package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * PC資産新規登録リクエスト DTO
 *
 * <p>POST /api/v1/assets のリクエストボディにマッピングされる。
 * バリデーションアノテーションで入力チェックを行い、通過したデータのみ登録処理に進む。</p>
 */
@Data
public class AssetCreateRequest {

    /** 資産番号（必須、ユニーク。例: "PC-2024-001"） */
    @NotBlank(message = "資産番号は必須です")
    private String assetNumber;

    /** 端末名・機種の通称（必須。例: "開発用ThinkPad"） */
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
     * 初期ステータス（デフォルト: IN_STORAGE）
     * 未指定の場合は「保管中」として登録される
     */
    private String status = "IN_STORAGE";

    /** 割り当て社員のID（任意、未割り当ての場合は null） */
    private Long assignedEmployeeId;

    /** ホスト名（任意。エージェント紐付け用） */
    private String hostname;

    /** 備考・メモ（任意） */
    private String note;
}
