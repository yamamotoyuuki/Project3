package com.company.pcmgmt.api.controller.common;

import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.common.CodeValueResponse;
import com.company.pcmgmt.service.common.CodeMasterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 共通 API コントローラー
 *
 * <p>複数画面で共通利用するユーティリティエンドポイントを提供する。
 * 主にドロップダウンリスト用のコードマスタ値取得に使用する。</p>
 *
 * <p>ベースパス: {@code /api/v1/common}</p>
 */
@RestController
@RequestMapping("/api/v1/common")
@RequiredArgsConstructor
public class CommonController {

    /** コードマスタ値取得サービス */
    private final CodeMasterService codeMasterService;

    /**
     * 指定したコード区分の有効なコード値一覧を取得する。
     *
     * <p>{@code code_master} テーブルからコード値と表示ラベルのペアのリストを返す。
     * {@code is_active = 1} のレコードのみを返し、{@code sort_order ASC} で並べ替える。</p>
     *
     * <p>エンドポイント: {@code GET /api/v1/common/codes/{codeType}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * <p>リクエスト例:
     * <pre>GET /api/v1/common/codes/PC_STATUS</pre>
     * </p>
     *
     * <p>レスポンス例:
     * <pre>
     * {
     *   "code": "SUCCESS",
     *   "message": "処理が完了しました",
     *   "data": [
     *     {"codeValue": "IN_STORAGE", "codeLabel": "保管中"},
     *     {"codeValue": "IN_USE",     "codeLabel": "使用中"},
     *     {"codeValue": "DISPOSED",   "codeLabel": "廃棄済み"}
     *   ]
     * }
     * </pre>
     * </p>
     *
     * <p>指定可能な codeType 一覧:
     * <ul>
     *   <li>{@code PC_STATUS}        - PCステータス（保管中 / 使用中 / 廃棄済み）</li>
     *   <li>{@code ACQUISITION_TYPE} - 取得区分（購入 / レンタル）</li>
     *   <li>{@code USER_ROLE}        - ユーザーロール（管理者 / オペレーター / 閲覧者）</li>
     *   <li>{@code AGENT_EVENT_TYPE} - エージェントイベント種別（登録 / 更新 / レポート送信）</li>
     *   <li>{@code OPERATION_TYPE}   - 操作ログ種別（登録 / 更新 / 削除 / エクスポート）</li>
     *   <li>{@code DEVICE_TYPE}      - 機器種別（ノートPC / デスクトップPC / モニター など）</li>
     * </ul>
     * </p>
     *
     * @param codeType コード区分キー（パスパラメータ、大文字・小文字を区別する）
     * @return {@link CodeValueResponse} のリストを {@link ApiResponse} でラップしたレスポンス
     */
    @GetMapping("/codes/{codeType}")
    public ResponseEntity<ApiResponse<List<CodeValueResponse>>> getCodeValues(
            @PathVariable("codeType") String codeType) {
        // 指定コード区分の有効なコード値を取得する
        List<CodeValueResponse> values = codeMasterService.findActiveByCodeType(codeType);
        return ResponseEntity.ok(ApiResponse.success(values));
    }
}
