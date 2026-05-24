package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.DashboardStatsResponse;
import com.company.pcmgmt.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ダッシュボードコントローラー
 *
 * <p>PC管理システムのダッシュボード画面に必要な統計情報を提供するエンドポイントを定義する。</p>
 *
 * <p>ベースパス: {@code /api/v1/dashboard}</p>
 */
@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    /** ダッシュボード統計集計を担うサービス */
    private final DashboardService dashboardService;

    /**
     * ダッシュボード統計情報を取得する
     *
     * <p>PC台数・貸出中件数・レンタル期限状況・ライセンス超過件数などの
     * サマリ情報をまとめて返す。</p>
     *
     * <p>エンドポイント: {@code GET /api/v1/dashboard/stats}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @return ダッシュボード統計レスポンス
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getStats() {
        // 統計情報を取得して共通レスポンスでラップして返す
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getStats()));
    }
}
