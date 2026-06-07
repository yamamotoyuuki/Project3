package com.company.pcmgmt.api.dto.request.agent;

import lombok.Data;

/**
 * エージェント初回登録リクエスト DTO
 *
 * <p>POST /api/v1/agent/register のリクエストボディにマッピングされる。
 * エージェントアプリの初回起動時（ローカルにエージェント番号が存在しない場合）に送信される。</p>
 *
 * <p>このエンドポイントは JWT 認証不要（SecurityConfig で permitAll 設定済み）。
 * 代わりに管理者が発行した enrollmentToken（24時間有効・1回限り）で認証する。</p>
 */
@Data
public class AgentRegisterRequest {

    /** エージェントが動作するPCのホスト名（エージェントが OS から自動取得） */
    private String hostname;

    /**
     * 登録トークン
     * <p>管理者がWebコンソールで発行した登録トークン（UUID形式）。
     * インストーラーまたは application.yml に事前設定してエージェントに渡す。
     * 有効期間は発行から24時間。使用は1回限り。</p>
     */
    private String enrollmentToken;
}
