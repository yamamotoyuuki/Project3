package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.request.SoftwareCreateRequest;
import com.company.pcmgmt.api.dto.request.SoftwareSearchRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.api.dto.response.SoftwareResponse;
import com.company.pcmgmt.service.SoftwareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * ソフトウェアライセンス管理コントローラー
 *
 * <p>ソフトウェアマスタの CRUD 操作エンドポイントを提供する。
 * ライセンス超過フィルターによる絞り込み検索もサポートする。</p>
 *
 * <p>ベースパス: {@code /api/v1/software}</p>
 */
@RestController
@RequestMapping("/api/v1/software")
@RequiredArgsConstructor
public class SoftwareController {

    /** ソフトウェア管理ビジネスロジックを担うサービス */
    private final SoftwareService softwareService;

    /**
     * ソフトウェア一覧を取得する（ページネーション・絞り込み対応）
     *
     * <p>エンドポイント: {@code GET /api/v1/software?page=0&size=20&keyword=xxx&overLimit=true}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 検索条件（クエリパラメータから自動バインド）
     * @return ページネーション付きソフトウェアレスポンス
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SoftwareResponse>>> findAll(SoftwareSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success(softwareService.findAll(req)));
    }

    /**
     * 指定IDのソフトウェア詳細を取得する
     *
     * <p>エンドポイント: {@code GET /api/v1/software/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id ソフトウェアマスタID（パスパラメータ）
     * @return ソフトウェアレスポンス（インストール数含む）
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SoftwareResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(softwareService.findById(id)));
    }

    /**
     * ソフトウェアを新規登録する
     *
     * <p>エンドポイント: {@code POST /api/v1/software}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 登録リクエスト（リクエストボディ、バリデーション適用）
     * @return HTTP 201 Created と登録後のソフトウェアレスポンス
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SoftwareResponse>> create(@Valid @RequestBody SoftwareCreateRequest req) {
        // ソフトウェアを登録してレスポンスを取得
        SoftwareResponse created = softwareService.create(req);
        // 201 Created で返す
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("ソフトウェアを登録しました", created));
    }

    /**
     * ソフトウェア情報を更新する（ライセンス数変更等）
     *
     * <p>エンドポイント: {@code PUT /api/v1/software/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id  更新対象のソフトウェアマスタID（パスパラメータ）
     * @param req 更新リクエスト（リクエストボディ、バリデーション適用）
     * @return 更新後のソフトウェアレスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SoftwareResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SoftwareCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("ソフトウェアを更新しました", softwareService.update(id, req)));
    }
}
