package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.annotation.Loggable;
import com.company.pcmgmt.api.dto.request.LoanCreateRequest;
import com.company.pcmgmt.api.dto.request.LoanReturnRequest;
import com.company.pcmgmt.api.dto.request.LoanSearchRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.LoanResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * PC貸出管理コントローラー
 *
 * <p>PC貸出の登録・返却・一覧取得エンドポイントを提供する。</p>
 *
 * <p>ベースパス: {@code /api/v1/loans}</p>
 */
@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    /** 貸出管理ビジネスロジックを担うサービス */
    private final LoanService loanService;

    /**
     * 貸出一覧を取得する（ページネーション・絞り込み対応）
     *
     * <p>エンドポイント: {@code GET /api/v1/loans}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 検索条件（クエリパラメータから自動バインド）
     * @return ページネーション付き貸出レスポンス
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LoanResponse>>> findAll(LoanSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success(loanService.findAll(req)));
    }

    /**
     * 指定IDの貸出記録詳細を取得する
     *
     * <p>エンドポイント: {@code GET /api/v1/loans/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id 貸出記録ID（パスパラメータ）
     * @return 貸出レスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.findById(id)));
    }

    /**
     * PC貸出を登録する
     *
     * <p>対象PCが既に貸出中の場合は HTTP 409 Conflict が返される。</p>
     *
     * <p>エンドポイント: {@code POST /api/v1/loans}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 貸出登録リクエスト（リクエストボディ、バリデーション適用）
     * @return HTTP 201 Created と登録後の貸出レスポンス
     */
    @Loggable(operation = "CREATE", targetType = "貸出")
    @PostMapping
    public ResponseEntity<ApiResponse<LoanResponse>> create(
            @Valid @RequestBody LoanCreateRequest req) {
        // 貸出を登録してレスポンスを取得
        LoanResponse created = loanService.create(req);
        // 201 Created で返す
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("貸出を登録しました", created));
    }

    /**
     * PC返却を登録する
     *
     * <p>既に返却済みの貸出IDに対してリクエストすると HTTP 409 Conflict が返される。</p>
     *
     * <p>エンドポイント: {@code PUT /api/v1/loans/{id}/return}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id  返却対象の貸出記録ID（パスパラメータ）
     * @param req 返却登録リクエスト（リクエストボディ、バリデーション適用）
     * @return 更新後の貸出レスポンス
     */
    @Loggable(operation = "RETURN", targetType = "貸出")
    @PutMapping("/{id}/return")
    public ResponseEntity<ApiResponse<LoanResponse>> returnLoan(
            @PathVariable Long id,
            @Valid @RequestBody LoanReturnRequest req) {
        return ResponseEntity.ok(ApiResponse.success("返却を登録しました", loanService.returnLoan(id, req)));
    }
}
