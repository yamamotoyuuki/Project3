package com.company.pcmgmt.api.dto.request.rental;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * レンタルベンダー登録・更新リクエスト DTO
 *
 * <p>POST /api/v1/rental-vendors および PUT /api/v1/rental-vendors/{id} の
 * リクエストボディにマッピングされる（登録・更新で同じ DTO を使用）。</p>
 */
@Data
public class RentalVendorCreateRequest {

    /** ベンダー（レンタル会社）の会社名（必須） */
    @NotBlank(message = "会社名は必須です")
    private String companyName;

    /** 担当者名（任意） */
    private String contactName;

    /** ベンダーの電話番号（任意） */
    private String phone;

    /** ベンダーのメールアドレス（任意） */
    private String email;

    /** ベンダーの住所（任意） */
    private String address;

    /** 備考・メモ（任意） */
    private String note;
}
