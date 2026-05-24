package com.company.pcmgmt.api.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * ソフトウェアマスタ登録・更新リクエスト DTO
 *
 * <p>POST /api/v1/software および PUT /api/v1/software/{id} の
 * リクエストボディにマッピングされる（登録・更新で同じ DTO を使用）。</p>
 */
@Data
public class SoftwareCreateRequest {

    /** ソフトウェア名（必須。例: "Microsoft Office 2021"） */
    @NotBlank(message = "ソフトウェア名は必須です")
    private String softwareName;

    /** 発行元・メーカー名（任意。例: "Microsoft"） */
    private String publisher;

    /** ライセンス種別（任意。例: "シートライセンス", "デバイスライセンス"） */
    private String licenseType;

    /**
     * 購入ライセンス数（0以上、デフォルト: 0）
     * 0の場合はライセンス数管理なし（超過チェックを行わない）
     */
    @Min(value = 0, message = "購入ライセンス数は0以上を指定してください")
    private Integer purchasedCount = 0;

    /** 備考・メモ（任意） */
    private String note;
}
