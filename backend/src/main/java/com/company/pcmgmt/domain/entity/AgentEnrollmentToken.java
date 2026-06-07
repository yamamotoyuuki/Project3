package com.company.pcmgmt.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * エージェント登録トークン エンティティ
 *
 * <p>管理者がWebコンソールで発行するエージェント初回登録用トークン。
 * 有効期間は発行から24時間。使用は1回限り。</p>
 *
 * <p>対応テーブル: {@code agent_enrollment_tokens}</p>
 */
@Data
public class AgentEnrollmentToken {

    /** レコードID（自動採番） */
    private Long id;

    /**
     * 登録トークン文字列（UUID形式）
     * <p>エージェントインストーラーに設定する値。1回限り有効。</p>
     */
    private String token;

    /** トークン有効期限（発行から24時間後） */
    private LocalDateTime expiresAt;

    /**
     * トークン使用日時
     * <p>使用済みの場合のみ設定される。null = 未使用。</p>
     */
    private LocalDateTime usedAt;

    /**
     * トークンを使用したエージェント番号
     * <p>使用済みの場合のみ設定される。null = 未使用。</p>
     */
    private String usedByAgentNumber;

    /** 発行メモ（対象PC名など任意メモ） */
    private String note;

    /** トークン発行者のユーザーID */
    private Long createdByUserId;

    /** 発行日時 */
    private LocalDateTime createdAt;
}
