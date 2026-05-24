package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * レンタルベンダーエンティティ
 *
 * <p>PCのレンタル契約先となるベンダー（リース会社）の情報を保持する。
 * 1つのベンダーに対して複数のレンタル契約が紐付く。</p>
 *
 * <p>対応テーブル: {@code rental_vendors}</p>
 */
@Data
public class RentalVendor {

    /** ベンダーID（主キー、自動採番） */
    private Long id;

    /** ベンダー（レンタル会社）の会社名（例: "NTTファイナンス株式会社"） */
    private String companyName;

    /** 担当者名（例: "山田 太郎"） */
    private String contactName;

    /** ベンダーの電話番号 */
    private String phone;

    /** ベンダーのメールアドレス */
    private String email;

    /** ベンダーの住所 */
    private String address;

    /** 備考・メモ（任意） */
    private String note;

    /** レコード作成日時（DB自動設定） */
    private LocalDateTime createdAt;

    /** レコード更新日時（DB自動設定） */
    private LocalDateTime updatedAt;
}
