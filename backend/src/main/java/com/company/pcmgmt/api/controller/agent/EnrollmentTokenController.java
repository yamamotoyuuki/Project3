package com.company.pcmgmt.api.controller.agent;

import com.company.pcmgmt.api.dto.request.agent.EnrollmentTokenRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.agent.EnrollmentTokenResponse;
import com.company.pcmgmt.security.JwtTokenProvider;
import com.company.pcmgmt.service.agent.EnrollmentTokenService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * エージェント登録トークン管理コントローラー
 *
 * <p>エージェントアプリの初回登録に使用するトークンを管理するエンドポイントを提供する。</p>
 *
 * <p>ベースパス: {@code /api/v1/agent-tokens}</p>
 *
 * <p>アクセス制御:
 * <ul>
 *   <li>トークン発行・一覧: ADMIN / IT_STAFF ロール</li>
 *   <li>トークン削除: ADMIN ロールのみ</li>
 * </ul>
 * </p>
 */
@RestController
@RequestMapping("/api/v1/agent-tokens")
@RequiredArgsConstructor
public class EnrollmentTokenController {

    /** 登録トークン管理サービス */
    private final EnrollmentTokenService enrollmentTokenService;

    /** JWT トークンプロバイダー（ログイン中ユーザーIDの取得に使用） */
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 登録トークンを新規発行する
     *
     * <p>エンドポイント: {@code POST /api/v1/agent-tokens}</p>
     * <p>認証: JWT 必須（ADMIN または IT_STAFF ロール）</p>
     *
     * @param req     発行リクエスト（任意メモ）
     * @param request HTTP リクエスト（JWT からユーザーIDを取得するために使用）
     * @return 発行したトークン情報
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
    public ResponseEntity<ApiResponse<EnrollmentTokenResponse>> issue(
            @RequestBody(required = false) EnrollmentTokenRequest req,
            HttpServletRequest request) {

        // JWT からログイン中ユーザーのIDを取得する
        Long userId = extractUserId(request);

        EnrollmentTokenResponse response = enrollmentTokenService.issue(req, userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 全登録トークンを一覧取得する
     *
     * <p>エンドポイント: {@code GET /api/v1/agent-tokens}</p>
     * <p>認証: JWT 必須（ADMIN または IT_STAFF ロール）</p>
     *
     * @return トークン一覧（発行日時の降順）
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'IT_STAFF')")
    public ResponseEntity<ApiResponse<List<EnrollmentTokenResponse>>> findAll() {
        List<EnrollmentTokenResponse> list = enrollmentTokenService.findAll();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    /**
     * 指定IDのトークンを削除（強制無効化）する
     *
     * <p>エンドポイント: {@code DELETE /api/v1/agent-tokens/{id}}</p>
     * <p>認証: JWT 必須（ADMIN ロールのみ）</p>
     *
     * @param id 削除するトークンのID
     * @return 処理成功レスポンス
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        enrollmentTokenService.delete(id);
        return ResponseEntity.ok(ApiResponse.success(null));
    }

    // =========================================================
    // プライベートユーティリティ
    // =========================================================

    /**
     * HTTP リクエストの Authorization ヘッダーから JWT を取り出し、ユーザーIDを返す
     *
     * @param request HTTP リクエスト
     * @return ログイン中ユーザーのID
     */
    private Long extractUserId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);
            return jwtTokenProvider.getUserId(token);
        }
        throw new IllegalStateException("Authorization ヘッダーが見つかりません");
    }
}
