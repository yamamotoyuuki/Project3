package com.company.pcmgmt.api.dto.request.agent;

import lombok.Data;

/**
 * エージェント登録トークン発行リクエスト DTO
 *
 * <p>POST /api/v1/agent-tokens のリクエストボディにマッピングされる。
 * IT担当者（OPERATOR）または管理者（ADMIN）が、エージェントをインストールする
 * 対象PCのためにトークンを発行する際に使用する。</p>
 */
@Data
public class EnrollmentTokenRequest {

    /**
     * 発行メモ（任意）
     * <p>対象PC名や用途などを記録するための任意メモ。
     * 管理画面のトークン一覧に表示する。</p>
     */
    private String note;
}
