package com.company.pcmgmt.api.dto.response.agent;

import lombok.Data;
import java.time.LocalDateTime;

/**
 * エージェント登録トークン レスポンス DTO
 *
 * <p>GET /api/v1/agent-tokens および POST /api/v1/agent-tokens のレスポンスに使用する。</p>
 */
@Data
public class EnrollmentTokenResponse {

    /** レコードID */
    private Long id;

    /**
     * トークン文字列（UUID形式）
     * <p>エージェントの application.yml に設定する値。</p>
     */
    private String token;

    /** トークン有効期限 */
    private LocalDateTime expiresAt;

    /**
     * トークン状態
     * <ul>
     *   <li>UNUSED  : 未使用・有効期限内</li>
     *   <li>USED    : 使用済み</li>
     *   <li>EXPIRED : 未使用だが有効期限切れ</li>
     * </ul>
     */
    private String status;

    /**
     * トークンを使用したエージェント番号
     * <p>使用済み（USED）の場合のみ設定される。</p>
     */
    private String usedByAgentNumber;

    /** 使用日時（使用済みの場合のみ設定） */
    private LocalDateTime usedAt;

    /** 発行メモ */
    private String note;

    /** 発行日時 */
    private LocalDateTime createdAt;
}
