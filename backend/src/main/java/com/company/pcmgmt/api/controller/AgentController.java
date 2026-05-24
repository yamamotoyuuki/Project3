package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.request.AgentReportRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * エージェント情報収集コントローラー
 *
 * <p>Tauri エージェント（クライアントPC上で動作するデスクトップアプリ）から
 * ハードウェア・ソフトウェア・ネットワーク情報を受信するエンドポイントを提供する。</p>
 *
 * <p>このコントローラーのエンドポイントは JWT 認証不要（SecurityConfig で permitAll 設定済み）。
 * ベースパス: {@code /api/v1/agent}</p>
 */
@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class AgentController {

    /** エージェント報告処理を担うサービス */
    private final AgentService agentService;

    /**
     * エージェントからのハードウェア・ソフトウェア・ネットワーク情報を受信して保存する
     *
     * <p>エージェントが収集した情報をまとめて受け取り、対象PC資産に紐付けてDBに保存する。
     * ホスト名でPC資産を検索するため、PC資産にホスト名が設定されている必要がある。</p>
     *
     * <p>エンドポイント: {@code POST /api/v1/agent/report}</p>
     * <p>認証: 不要（エージェントからの自動実行のため）</p>
     *
     * @param req エージェント報告リクエスト（ホスト名・ハードウェア・ソフトウェア・ネットワーク情報）
     * @return 処理結果コード（"OK", "HOSTNAME_MISSING", "ASSET_NOT_FOUND"）
     */
    @PostMapping("/report")
    public ResponseEntity<ApiResponse<String>> report(
            @RequestBody AgentReportRequest req) {
        // エージェント報告を処理して結果コードを取得
        String result = agentService.processReport(req);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
