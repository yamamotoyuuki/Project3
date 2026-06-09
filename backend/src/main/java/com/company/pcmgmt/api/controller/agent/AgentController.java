package com.company.pcmgmt.api.controller.agent;

import com.company.pcmgmt.api.dto.request.agent.AgentRegisterRequest;
import com.company.pcmgmt.api.dto.request.agent.AgentReportRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.agent.AgentRegisterResponse;
import com.company.pcmgmt.api.dto.response.agent.AssetInfoResponse;
import com.company.pcmgmt.service.agent.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * エージェント情報収集コントローラー
 *
 * <p>Tauri エージェント（クライアントPC上で動作するデスクトップアプリ）から
 * ハードウェア・ソフトウェア・ネットワーク情報を受信するエンドポイントを提供する。</p>
 *
 * <p>認証方式:
 * <ul>
 *   <li>/register : JWT 不要。登録トークン（リクエストボディの enrollmentToken）で認証。</li>
 *   <li>/report   : JWT 不要。Authorization: Bearer {apiKey} ヘッダーで認証。</li>
 *   <li>/asset-info: JWT 不要。Authorization: Bearer {apiKey} ヘッダーで認証。</li>
 * </ul>
 * </p>
 *
 * <p>ベースパス: {@code /api/v1/agent}</p>
 */
@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class AgentController {

    /** エージェント報告処理を担うサービス */
    private final AgentService agentService;

    /** Authorization ヘッダーのプレフィックス */
    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * エージェント起動時にPC資産の取得区分（購入/レンタル）を返す
     *
     * <p>エンドポイント: {@code GET /api/v1/agent/asset-info?agentNumber={}&hostname={}}</p>
     * <p>認証: Authorization: Bearer {apiKey} ヘッダー必須</p>
     *
     * @param agentNumber   エージェント番号（クエリパラメータ）
     * @param hostname      フォールバック検索用ホスト名（クエリパラメータ）
     * @param authorization Authorization ヘッダー（Bearer {apiKey} 形式）
     * @return 取得区分文字列（"PURCHASE" / "RENTAL"）、未登録・未設定の場合は null
     */
    @GetMapping("/asset-info")
    public ResponseEntity<ApiResponse<AssetInfoResponse>> getAssetInfo(
            @RequestParam("agentNumber") String agentNumber,
            @RequestParam(value = "hostname", required = false) String hostname,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // APIキーを抽出して検証する
        String apiKey = extractBearerToken(authorization);
        agentService.validateApiKey(agentNumber, apiKey);

        // エージェント番号（+ホスト名フォールバック）でPC資産を検索して取得区分と返却済みフラグを返す
        AssetInfoResponse assetInfo = agentService.getAssetInfo(agentNumber, hostname);
        return ResponseEntity.ok(ApiResponse.success(assetInfo));
    }

    /**
     * エージェントの初回登録を行い、エージェント番号とAPIキーを発行して返す
     *
     * <p>エンドポイント: {@code POST /api/v1/agent/register}</p>
     * <p>認証: リクエストボディの enrollmentToken（24時間有効・1回限り）で認証</p>
     *
     * @param req エージェント初回登録リクエスト（ホスト名・登録トークンを含む）
     * @return 発行したエージェント番号とAPIキーを含むレスポンス
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AgentRegisterResponse>> register(
            @RequestBody AgentRegisterRequest req) {
        // エージェントを新規登録してエージェント番号とAPIキーを取得
        AgentRegisterResponse response = agentService.register(req);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * エージェントからのハードウェア・ソフトウェア・ネットワーク情報を受信して保存する
     *
     * <p>エンドポイント: {@code POST /api/v1/agent/report}</p>
     * <p>認証: Authorization: Bearer {apiKey} ヘッダー必須</p>
     *
     * @param req           エージェント報告リクエスト
     * @param authorization Authorization ヘッダー（Bearer {apiKey} 形式）
     * @return 処理結果コード（"OK", "HOSTNAME_MISSING", "ASSET_NOT_FOUND"）
     */
    @PostMapping("/report")
    public ResponseEntity<ApiResponse<String>> report(
            @RequestBody AgentReportRequest req,
            @RequestHeader(value = "Authorization", required = false) String authorization) {

        // APIキーを抽出して検証する（agentNumber はリクエストボディから取得）
        String apiKey = extractBearerToken(authorization);
        agentService.validateApiKey(req.getAgentNumber(), apiKey);

        // エージェント報告を処理して結果コードを取得
        String result = agentService.processReport(req);
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    // =========================================================
    // プライベートユーティリティ
    // =========================================================

    /**
     * Authorization ヘッダーから Bearer トークン（APIキー）を抽出する
     *
     * @param authorization Authorization ヘッダーの値（例: "Bearer abc123..."）
     * @return APIキー文字列（Bearer プレフィックスを除いた部分）
     * @throws IllegalArgumentException ヘッダーが null / Bearer 形式でない場合
     */
    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw new IllegalArgumentException("APIキーが指定されていません（Authorization: Bearer {apiKey} が必要です）");
        }
        return authorization.substring(BEARER_PREFIX.length()).trim();
    }
}
