package com.company.pcmgmt.api.controller;

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

@RestController
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class PcAssetController {

    private final PcAssetService pcAssetService;

    /**
     * PC資産一覧取得
     * GET /api/v1/assets?page=0&size=20&status=IN_USE&keyword=xxx
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AssetResponse>>> findAll(
            AssetSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success(pcAssetService.findAll(req)));
    }

    /**
     * PC資産詳細取得
     * GET /api/v1/assets/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(pcAssetService.findById(id)));
    }

    /**
     * PC資産登録
     * POST /api/v1/assets
     */
    @PostMapping
    public ResponseEntity<ApiResponse<AssetResponse>> create(
            @Valid @RequestBody AssetCreateRequest req) {
        AssetResponse created = pcAssetService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("PC資産を登録しました", created));
    }

    /**
     * PC資産更新
     * PUT /api/v1/assets/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AssetResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody AssetUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("PC資産を更新しました", pcAssetService.update(id, req)));
    }

    /**
     * PC資産削除
     * DELETE /api/v1/assets/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        pcAssetService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("PC資産を削除しました", null));
    }
}
