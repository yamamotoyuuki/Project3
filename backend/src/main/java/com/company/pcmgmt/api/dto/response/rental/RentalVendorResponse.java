package com.company.pcmgmt.api.dto.response.rental;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * レンタルベンダーレスポンス DTO
 *
 * <p>ベンダー一覧・詳細 API が返却するデータ構造。
 * {@code null} フィールドは JSON 出力から除外される（{@code @JsonInclude(NON_NULL)}）。</p>
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RentalVendorResponse {

    /** ベンダーID */
    private Long id;

    /** ベンダー（レンタル会社）の会社名 */
    private String companyName;

    /** 担当者名 */
    private String contactName;

    /** ベンダーの電話番号 */
    private String phone;

    /** ベンダーのメールアドレス */
    private String email;

    /** ベンダーの住所 */
    private String address;

    /** 備考・メモ */
    private String note;

    /** レコード作成日時 */
    private LocalDateTime createdAt;

    /** レコード更新日時 */
    private LocalDateTime updatedAt;
}
