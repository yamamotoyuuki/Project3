package com.company.pcmgmt.api.controller.agent;

import com.company.pcmgmt.api.dto.request.agent.AgentRegisterRequest;
import com.company.pcmgmt.api.dto.request.agent.AgentReportRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
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
     * エージェント起動時にPC資産の取得区分（購入/レンタル）を返す
     *
     * <p>エージェントアプリ起動時に呼び出され、バックエンドに登録済みの取得区分を返す。
     * バックエンドに取得区分が設定済みの場合、エージェント側の選択欄を読み取り専用にするために使用する。</p>
     *
     * <p>検索順序:
     * <ol>
     *   <li>エージェント番号で検索（{@code pc_assets.agent_number} が設定済みの場合）</li>
     *   <li>ホスト名でフォールバック検索（管理者が先に資産を登録した場合など、
     *       {@code pc_assets.agent_number} が未設定でもホスト名が一致すれば取得できる）</li>
     * </ol>
     * </p>
     *
     * <p>エンドポイント: {@code GET /api/v1/agent/asset-info?agentNumber={agentNumber}&hostname={hostname}}</p>
     * <p>認証: 不要（エージェントからの自動実行のため）</p>
     *
     * @param agentNumber エージェント番号（クエリパラメータ、例: "AGT-A1B2C3D4"）
     * @param hostname    エージェントのホスト名（フォールバック検索用、例: "LAPTOP-3N4AQL6D"）
     * @return 取得区分文字列（"PURCHASE" / "RENTAL"）、未登録・未設定の場合は null
     */
    @GetMapping("/asset-info")
    public ResponseEntity<ApiResponse<String>> getAssetInfo(
            @RequestParam("agentNumber") String agentNumber,
            @RequestParam(value = "hostname", required = false) String hostname) {
        // エージェント番号（＋ホスト名フォールバック）でPC資産を検索して取得区分を返す
        String acquisitionType = agentService.getAcquisitionType(agentNumber, hostname);
        return ResponseEntity.ok(ApiResponse.success(acquisitionType));
    }

    /**
     * エージェントの初回登録を行い、エージェント番号を発行して返す
     *
     * <p>エージェントアプリの初回起動時（ローカルにエージェント番号が存在しない場合）に呼び出される。
     * UUID から "AGT-XXXXXXXX" 形式のエージェント番号を生成して agents テーブルに登録する。</p>
     *
     * <p>エンドポイント: {@code POST /api/v1/agent/register}</p>
     * <p>認証: 不要（エージェントからの初回登録のため）</p>
     *
     * @param req エージェント初回登録リクエスト（ホスト名を含む）
     * @return 発行したエージェント番号（例: "AGT-A1B2C3D4"）
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(
            @RequestBody AgentRegisterRequest req) {
        // エージェントを新規登録してエージェント番号を取得
        String agentNumber = agentService.register(req);
        return ResponseEntity.ok(ApiResponse.success(agentNumber));
    }

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
