package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * システムユーザーエンティティ
 *
 * <p>PC管理システムにログインして操作を行うユーザー情報を保持する。
 * ロールは ADMIN（管理者）/ IT_STAFF（IT担当者）/ VIEWER（閲覧者）の3種類。</p>
 *
 * <p>対応テーブル: {@code users}</p>
 */
@Data
public class User {

    /** ユーザーID（主キー、自動採番） */
    private Long id;

    /** ログインユーザー名（ユニーク制約あり） */
    private String username;

    /** パスワードハッシュ値（BCrypt でハッシュ化して保存） */
    private String passwordHash;

    /** 画面に表示するユーザーの表示名 */
    private String displayName;

    /** ユーザーロール（ADMIN / IT_STAFF / VIEWER） */
    private String role;

    /** ユーザーのメールアドレス（任意） */
    private String email;

    /** アカウント有効フラグ（true: 有効, false: 無効） */
    private Boolean isActive;

    /** 最終ログイン日時 */
    private LocalDateTime lastLoginAt;

    /** レコード作成日時（DB自動設定） */
    private LocalDateTime createdAt;

    /** レコード更新日時（DB自動設定） */
    private LocalDateTime updatedAt;
}
