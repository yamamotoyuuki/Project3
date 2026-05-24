package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.request.EmployeeCreateRequest;
import com.company.pcmgmt.api.dto.request.EmployeeSearchRequest;
import com.company.pcmgmt.api.dto.request.EmployeeUpdateRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.EmployeeResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 社員管理コントローラー
 *
 * <p>社員情報の CRUD 操作エンドポイントを提供する。</p>
 *
 * <p>ベースパス: {@code /api/v1/employees}</p>
 */
@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    /** 社員管理ビジネスロジックを担うサービス */
    private final EmployeeService employeeService;

    /**
     * 社員一覧を取得する（ページネーション・絞り込み対応）
     *
     * <p>クエリパラメータで絞り込み条件を指定できる。</p>
     *
     * <p>エンドポイント: {@code GET /api/v1/employees?page=0&size=20&keyword=xxx&isActive=true}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 検索条件（クエリパラメータから自動バインド）
     * @return ページネーション付き社員レスポンス
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> findAll(
            EmployeeSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.findAll(req)));
    }

    /**
     * 在籍中の社員リストを取得する（プルダウン選択用）
     *
     * <p>PC資産の担当者割り当てや貸出先選択ドロップダウンで使用する。
     * isActive = true の社員のみを返す。</p>
     *
     * <p>エンドポイント: {@code GET /api/v1/employees/active}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @return 在籍中社員のリスト
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> findActiveList() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.findActiveList()));
    }

    /**
     * 指定IDの社員詳細を取得する
     *
     * <p>エンドポイント: {@code GET /api/v1/employees/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id 社員ID（パスパラメータ）
     * @return 社員レスポンス
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.findById(id)));
    }

    /**
     * 社員を新規登録する
     *
     * <p>エンドポイント: {@code POST /api/v1/employees}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 登録リクエスト（リクエストボディ、バリデーション適用）
     * @return HTTP 201 Created と登録後の社員レスポンス
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(
            @Valid @RequestBody EmployeeCreateRequest req) {
        // 社員を登録してレスポンスを取得
        EmployeeResponse created = employeeService.create(req);
        // 201 Created で返す
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("社員を登録しました", created));
    }

    /**
     * 社員情報を更新する
     *
     * <p>退職処理は {@code isActive=false} を含むリクエストで行う。</p>
     *
     * <p>エンドポイント: {@code PUT /api/v1/employees/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id  更新対象の社員ID（パスパラメータ）
     * @param req 更新リクエスト（リクエストボディ、バリデーション適用）
     * @return 更新後の社員レスポンス
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("社員情報を更新しました", employeeService.update(id, req)));
    }
}
