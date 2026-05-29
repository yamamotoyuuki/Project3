package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.request.RentalCreateRequest;
import com.company.pcmgmt.api.dto.request.RentalSearchRequest;
import com.company.pcmgmt.api.dto.request.RentalVendorCreateRequest;
import com.company.pcmgmt.api.dto.response.*;
import com.company.pcmgmt.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * レンタル管理コントローラー
 *
 * <p>レンタル契約とレンタルベンダーの CRUD 操作エンドポイントを提供する。
 * 2つのリソースを1つのコントローラーで管理するため、{@code @RequestMapping} は使用せず
 * 各メソッドにフルパスを直接指定している。</p>
 *
 * <p>レンタル契約: {@code /api/v1/rentals}</p>
 * <p>レンタルベンダー: {@code /api/v1/rental-vendors}</p>
 */
@RestController
@RequiredArgsConstructor
public class RentalController {

    /** レンタル管理ビジネスロジックを担うサービス */
    private final RentalService rentalService;

    // ======= レンタル契約 /api/v1/rentals =======

    /**
     * レンタル契約一覧を取得する（ページネーション・絞り込み対応）
     *
     * <p>エンドポイント: {@code GET /api/v1/rentals}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 検索条件（クエリパラメータから自動バインド）
     * @return ページネーション付きレンタル契約レスポンス
     */
    @GetMapping("/api/v1/rentals")
    public ResponseEntity<ApiResponse<PageResponse<RentalResponse>>> findAll(RentalSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.findAll(req)));
    }

    /**
     * 指定IDのレンタル契約詳細を取得する
     *
     * <p>エンドポイント: {@code GET /api/v1/rentals/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id レンタル契約ID（パスパラメータ）
     * @return レンタル契約レスポンス
     */
    @GetMapping("/api/v1/rentals/{id}")
    public ResponseEntity<ApiResponse<RentalResponse>> findById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.findById(id)));
    }

    /**
     * レンタル契約を新規登録する
     *
     * <p>エンドポイント: {@code POST /api/v1/rentals}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 登録リクエスト（リクエストボディ、バリデーション適用）
     * @return HTTP 201 Created と登録後のレンタル契約レスポンス
     */
    @PostMapping("/api/v1/rentals")
    public ResponseEntity<ApiResponse<RentalResponse>> create(@Valid @RequestBody RentalCreateRequest req) {
        // レンタル契約を登録してレスポンスを取得
        RentalResponse created = rentalService.create(req);
        // 201 Created で返す
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("レンタル契約を登録しました", created));
    }

    /**
     * レンタル品の返却を登録する
     *
     * <p>返却日として本日の日付が自動セットされる。
     * 既に返却済みの場合は HTTP 409 Conflict が返される。</p>
     *
     * <p>エンドポイント: {@code PUT /api/v1/rentals/{id}/return}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id 返却対象のレンタル契約ID（パスパラメータ）
     * @return 更新後のレンタル契約レスポンス
     */
    @PutMapping("/api/v1/rentals/{id}/return")
    public ResponseEntity<ApiResponse<RentalResponse>> returnRental(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success("返却を登録しました", rentalService.returnRental(id)));
    }

    // ======= ベンダー /api/v1/rental-vendors =======

    /**
     * 全レンタルベンダーを取得する
     *
     * <p>エンドポイント: {@code GET /api/v1/rental-vendors}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @return ベンダーレスポンスのリスト
     */
    @GetMapping("/api/v1/rental-vendors")
    public ResponseEntity<ApiResponse<List<RentalVendorResponse>>> findAllVendors() {
        return ResponseEntity.ok(ApiResponse.success(rentalService.findAllVendors()));
    }

    /**
     * 指定IDのベンダー詳細を取得する
     *
     * <p>エンドポイント: {@code GET /api/v1/rental-vendors/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id ベンダーID（パスパラメータ）
     * @return ベンダーレスポンス
     */
    @GetMapping("/api/v1/rental-vendors/{id}")
    public ResponseEntity<ApiResponse<RentalVendorResponse>> findVendorById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.findVendorById(id)));
    }

    /**
     * レンタルベンダーを新規登録する
     *
     * <p>エンドポイント: {@code POST /api/v1/rental-vendors}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param req 登録リクエスト（リクエストボディ、バリデーション適用）
     * @return HTTP 201 Created と登録後のベンダーレスポンス
     */
    @PostMapping("/api/v1/rental-vendors")
    public ResponseEntity<ApiResponse<RentalVendorResponse>> createVendor(
            @Valid @RequestBody RentalVendorCreateRequest req) {
        // ベンダーを登録してレスポンスを取得
        RentalVendorResponse created = rentalService.createVendor(req);
        // 201 Created で返す
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("ベンダーを登録しました", created));
    }

    /**
     * レンタルベンダー情報を更新する
     *
     * <p>エンドポイント: {@code PUT /api/v1/rental-vendors/{id}}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @param id  更新対象のベンダーID（パスパラメータ）
     * @param req 更新リクエスト（リクエストボディ、バリデーション適用）
     * @return 更新後のベンダーレスポンス
     */
    @PutMapping("/api/v1/rental-vendors/{id}")
    public ResponseEntity<ApiResponse<RentalVendorResponse>> updateVendor(
            @PathVariable("id") Long id,
            @Valid @RequestBody RentalVendorCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("ベンダーを更新しました", rentalService.updateVendor(id, req)));
    }
}
