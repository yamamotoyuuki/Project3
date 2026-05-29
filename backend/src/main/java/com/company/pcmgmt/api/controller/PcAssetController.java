package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.annotation.Loggable;
import com.company.pcmgmt.api.dto.request.AssetCreateRequest;
import com.company.pcmgmt.api.dto.request.AssetSearchRequest;
import com.company.pcmgmt.api.dto.request.AssetUpdateRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.AssetResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.service.PcAssetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PC資産管理コントローラー
 *
 * <p>PC資産の CRUD 操作エンドポイントを提供する。</p>
 *
 * <p>ベースパス: {@code /api/v1/assets}</p>
 */
@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class PcAssetController {

    /** PC資産管理ビジネスロジックを担うサービス */
    private final PcAssetService pcAssetService;

    /**
     * PC資産一覧を取得する（ページネーション・絞り込み対応）
     *
     * <p>クエリパラメータでステータス・取得区分・キーワードによる絞り込みが可能。</p>
     *
     * <p>エンドポイント: {@code GET /api/v1/assets?page=0&size=20&status=IN_USE&keyword=xxx}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 検索条件（クエリパラメータから自動バインド）
     * @return ページネーション付きPC資産レスポンス
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AssetResponse>>> findAll(
            AssetSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success(pcAssetService.findAll(req)));
    }

    /**
     * 指定IDのPC資産詳細を取得する
     *
     * <p>エンドポイント: {@code GET /api/v1/assets/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id PC資産ID（パスパラメータ）
     * @return PC資産レスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(pcAssetService.findById(id)));
    }

    /**
     * PC資産を新規登録する
     *
     * <p>資産番号が既存のものと重複する場合は HTTP 400 Bad Request が返される。</p>
     *
     * <p>エンドポイント: {@code POST /api/v1/assets}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 登録リクエスト（リクエストボディ、バリデーション適用）
     * @return HTTP 201 Created と登録後のPC資産レスポンス
     */
    @Loggable(operation = "CREATE", targetType = "PC資産")
    @PostMapping
    public ResponseEntity<ApiResponse<AssetResponse>> create(
            @Valid @RequestBody AssetCreateRequest req) {
        // PC資産を登録してレスポンスを取得
        AssetResponse created = pcAssetService.create(req);
        // 201 Created で返す
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("PC資産を登録しました", created));
    }

    /**
     * PC資産を更新する
     *
     * <p>ステータス変更・担当社員の変更もこのエンドポイントで行う。</p>
     *
     * <p>エンドポイント: {@code PUT /api/v1/assets/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id  更新対象のPC資産ID（パスパラメータ）
     * @param req 更新リクエスト（リクエストボディ、バリデーション適用）
     * @return 更新後のPC資産レスポンス
     */
    @Loggable(operation = "UPDATE", targetType = "PC資産")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> update(
            @PathVariable("id") Long id,
            @Valid @RequestBody AssetUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("PC資産を更新しました", pcAssetService.update(id, req)));
    }

    /**
     * PC資産を削除する（物理削除）
     *
     * <p>廃棄処理に使用する。論理削除したい場合はステータスを DISPOSED に更新すること。</p>
     *
     * <p>エンドポイント: {@code DELETE /api/v1/assets/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id 削除対象のPC資産ID（パスパラメータ）
     * @return HTTP 200 OK と削除完了メッセージ
     */
    @Loggable(operation = "DELETE", targetType = "PC資産")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable("id") Long id) {
        pcAssetService.delete(id);
        // 削除成功時は data=null で返す
        return ResponseEntity.ok(ApiResponse.success("PC資産を削除しました", null));
    }
}
