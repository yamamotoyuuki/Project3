package com.company.pcmgmt.exception;

import com.company.pcmgmt.api.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.stream.Collectors;

/**
 * グローバル例外ハンドラー
 *
 * <p>コントローラー層で発生した例外をキャッチし、統一フォーマット（{@link ApiResponse}）で
 * クライアントにエラーレスポンスを返す。
 * {@code @RestControllerAdvice} により全コントローラーに適用される。</p>
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 認証エラー（ユーザー名またはパスワードが不正）のハンドリング
     *
     * @param e 認証失敗例外
     * @return HTTP 401 Unauthorized
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Void>> handleBadCredentials(BadCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("UNAUTHORIZED", e.getMessage()));
    }

    /**
     * アクセス権限エラーのハンドリング
     *
     * <p>認証済みユーザーが権限のないリソースにアクセスした場合（例: 非ADMINが /users に）。</p>
     *
     * @param e アクセス拒否例外
     * @return HTTP 403 Forbidden
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error("FORBIDDEN", "アクセス権限がありません"));
    }

    /**
     * バリデーションエラーのハンドリング
     *
     * <p>リクエストボディのバリデーション（{@code @Valid}）で複数のフィールドエラーが
     * 発生した場合、全エラーメッセージをカンマ区切りで返す。</p>
     *
     * @param e バリデーション例外
     * @return HTTP 400 Bad Request
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationErrors(
            MethodArgumentNotValidException e) {
        // 全フィールドエラーのメッセージをカンマ区切りで結合
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("VALIDATION_ERROR", message));
    }

    /**
     * リソース未検出エラーのハンドリング
     *
     * <p>指定IDのリソースが DB に存在しない場合（例: 存在しないPC資産IDを指定）。</p>
     *
     * @param e リソース未検出例外
     * @return HTTP 404 Not Found
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(
            ResourceNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", e.getMessage()));
    }

    /**
     * 不正な引数エラーのハンドリング
     *
     * <p>重複チェックなど、業務ロジックで引数が不正と判断された場合（例: 資産番号が重複）。</p>
     *
     * @param e 不正引数例外
     * @return HTTP 400 Bad Request
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("BAD_REQUEST", e.getMessage()));
    }

    /**
     * 不正な状態エラーのハンドリング
     *
     * <p>業務ロジック上、操作が許可されない状態の場合（例: 貸出中PCへの二重貸出、返却済みへの再返却）。</p>
     *
     * @param e 不正状態例外
     * @return HTTP 409 Conflict
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error("CONFLICT", e.getMessage()));
    }

    /**
     * 静的リソース未検出エラーのハンドリング（例: 存在しないURL）
     *
     * @param e 静的リソース未検出例外
     * @return HTTP 404 Not Found
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResource(NoResourceFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("NOT_FOUND", "リソースが見つかりません: " + e.getResourcePath()));
    }

    /**
     * 予期しない例外のハンドリング（フォールバック）
     *
     * <p>上記のハンドラーで捕捉されなかった全ての例外をここで受け取る。
     * エラーの詳細はサーバーログに出力するが、クライアントには汎用メッセージのみ返す。</p>
     *
     * @param e 予期しない例外
     * @return HTTP 500 Internal Server Error
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGeneral(Exception e) {
        // サーバー側に詳細なスタックトレースを出力
        log.error("予期しないエラーが発生しました", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("INTERNAL_ERROR", "サーバーエラーが発生しました"));
    }
}
