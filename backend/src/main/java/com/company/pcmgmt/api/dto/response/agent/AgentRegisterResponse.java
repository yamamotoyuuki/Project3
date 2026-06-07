package com.company.pcmgmt.api.dto.response.agent;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * エージェント初回登録レスポンス DTO
 *
 * <p>POST /api/v1/agent/register のレスポンスボディにマッピングされる。</p>
 *
 * <p>登録成功時にエージェント番号とAPIキーの両方を返す。
 * APIキーはこの一度だけ平文で返却される。エージェントはセキュアな場所に保存すること。</p>
 */
@Data
@AllArgsConstructor
public class AgentRegisterResponse {

    /**
     * 発行したエージェント番号
     * <p>形式: "AGT-XXXXXXXX"（UUID先頭8文字、大文字）</p>
     */
    private String agentNumber;

    /**
     * 発行したAPIキー（平文）
     * <p>この値はDBに保存されない（bcryptハッシュのみ保存）。
     * エージェントはこの値を安全な場所（隠しファイル）に保存し、
     * 以降のAPIリクエストの Authorization ヘッダーに Bearer トークンとして付与する。</p>
     */
    private String apiKey;
}
