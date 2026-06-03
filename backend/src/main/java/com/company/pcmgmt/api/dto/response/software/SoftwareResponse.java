package com.company.pcmgmt.api.dto.response.software;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * ソフトウェアレスポンス DTO
 *
 * <p>ソフトウェア一覧・詳細 API が返却するデータ構造。
 * インストール済み台数（{@code installedCount}）は pc_software_info テーブルから集計して取得する。
 * {@code null} フィールドは JSON 出力から除外される（{@code @JsonInclude(NON_NULL)}）。</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SoftwareResponse {

    /** ソフトウェアマスタID */
    private Long id;

    /** ソフトウェア名（例: "Microsoft Office 2021"） */
    private String softwareName;

    /** 発行元・メーカー名 */
    private String publisher;

    /** ライセンス種別（例: "シートライセンス"） */
    private String licenseType;

    /** 購入ライセンス数（0 の場合はライセンス数管理なし） */
    private Integer purchasedCount;

    /**
     * インストール済み台数（pc_software_info テーブルからの集計値）
     * エージェントが未導入の環境では null または 0 になる
     */
    private Integer installedCount;

    /** 備考・メモ */
    private String note;

    /** レコード作成日時 */
    private LocalDateTime createdAt;

    /** レコード更新日時 */
    private LocalDateTime updatedAt;

    /**
     * ライセンス超過かどうかを判定する
     *
     * <p>購入ライセンス数が 0 または未設定の場合は管理対象外として false を返す。
     * インストール数が購入数を超えている場合に true を返す。</p>
     *
     * @return ライセンス超過の場合は true
     */
    public boolean isOverLimit() {
        // 購入数が 0 または未設定の場合はライセンス管理対象外（超過チェックしない）
        if (purchasedCount == null || purchasedCount == 0) return false;
        // インストール数が購入数を超えている場合は超過
        return installedCount != null && installedCount > purchasedCount;
    }
}
