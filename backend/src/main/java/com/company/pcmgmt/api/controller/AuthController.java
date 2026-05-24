package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.request.LoginRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.LoginResponse;
import com.company.pcmgmt.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 認証コントローラー
 *
 * <p>ログイン・ログアウトエンドポイントを提供する。
 * このコントローラーのエンドポイントは JWT 認証不要（SecurityConfig で permitAll 設定済み）。</p>
 *
 * <p>ベースパス: {@code /api/v1/auth}</p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    /** 認証処理（ログイン・JWT発行）を担うサービス */
    private final AuthService authService;

    /**
     * ログイン認証を行い JWT トークンを発行する
     *
     * <p>ユーザー名とパスワードを受け取り、認証が成功した場合に JWT トークンを返す。
     * 認証失敗時は HTTP 401 Unauthorized が返される。</p>
     *
     * <p>エンドポイント: {@code POST /api/v1/auth/login}</p>
     * <p>認証: 不要（認証前のエンドポイント）</p>
     *
     * @param request ログインリクエスト（ユーザー名・パスワード、バリデーション適用）
     * @return ログインレスポンス（JWT トークン・ユーザー情報）
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        // 認証処理を実行し、JWT トークンを含むレスポンスを取得
        LoginResponse loginResponse = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success("ログインしました", loginResponse));
    }

    /**
     * ログアウト処理（サーバーサイドは確認のみ）
     *
     * <p>JWT 認証ではサーバーサイドでセッションを管理しないため、
     * フロントエンド側でローカルストレージ等に保存したトークンを削除することでログアウトを実現する。
     * このエンドポイントはログアウト確認レスポンスを返すのみ。</p>
     *
     * <p>エンドポイント: {@code POST /api/v1/auth/logout}</p>
     * <p>認証: JWT 認証必須（ログイン中ユーザーのみ呼び出し可能）</p>
     *
     * @return ログアウト完了メッセージ
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // サーバーサイドでの処理なし（フロント側でトークンを削除する）
        return ResponseEntity.ok(ApiResponse.success("ログアウトしました", null));
    }
}
