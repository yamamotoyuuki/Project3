package com.company.pcmgmt.api.dto.request;

import lombok.Data;

/**
 * エージェント初回登録リクエスト DTO
 *
 * <p>POST /api/v1/agent/register のリクエストボディにマッピングされる。
 * エージェントアプリの初回起動時（ローカルにエージェント番号が存在しない場合）に送信される。</p>
 *
 * <p>このエンドポイントは JWT 認証不要（SecurityConfig で permitAll 設定済み）。</p>
 */
@Data
public class AgentRegisterRequest {

    /** エージェントが動作するPCのホスト名（エージェントが OS から自動取得） */
    private String hostname;
}
