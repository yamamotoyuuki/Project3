package com.company.pcmgmt.interceptor;

import com.company.pcmgmt.annotation.Loggable;
import com.company.pcmgmt.domain.entity.OperationLog;
import com.company.pcmgmt.service.OperationLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 操作ログ記録インターセプター
 *
 * <p>Spring MVC の {@link HandlerInterceptor} として動作し、
 * {@link Loggable} アノテーションが付与されたコントローラーメソッドの
 * <strong>リクエスト正常終了後</strong>（afterCompletion）に操作ログを記録する。</p>
 *
 * <p>追加ライブラリ不要: spring-web に含まれる {@link HandlerInterceptor} を使用する。</p>
 *
 * <p>記録タイミング:</p>
 * <ul>
 *   <li>{@code afterCompletion}: HTTP ステータスが 2xx の場合のみログを記録する。
 *       エラー（4xx/5xx）が発生した操作はログに残さない。</li>
 * </ul>
 *
 * <p>登録場所: {@link com.company.pcmgmt.config.WebMvcConfig}</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLoggingInterceptor implements HandlerInterceptor {

    /** 操作ログを非同期で DB に書き込むサービス */
    private final OperationLogService operationLogService;

    /**
     * リクエスト処理完了後にログを記録する
     *
     * <p>ポイント:</p>
     * <ul>
     *   <li>ハンドラーが {@link HandlerMethod} でない場合（静的リソース等）はスキップする</li>
     *   <li>コントローラーメソッドに {@link Loggable} アノテーションがない場合はスキップする</li>
     *   <li>HTTP ステータスが 200 番台の場合のみログを記録する（失敗操作は記録しない）</li>
     * </ul>
     *
     * @param request  HTTP リクエスト
     * @param response HTTP レスポンス
     * @param handler  呼び出されたハンドラー（コントローラーメソッド等）
     * @param ex       処理中に発生した例外（null = 正常終了）
     */
    @Override
    public void afterCompletion(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull Object handler,
            Exception ex) {

        // ---- ① HandlerMethod かどうかを確認 ----
        // 静的リソース等の場合は HandlerMethod ではないのでスキップ
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return;
        }

        // ---- ② @Loggable アノテーションの存在確認 ----
        Loggable loggable = handlerMethod.getMethodAnnotation(Loggable.class);
        if (loggable == null) {
            return; // @Loggable がないメソッドはスキップ
        }

        // ---- ③ HTTP ステータスが 2xx の場合のみログを記録 ----
        int status = response.getStatus();
        if (status < 200 || status >= 300) {
            return; // エラーレスポンスはログを記録しない
        }

        try {
            // ---- ④ ログインユーザーの情報を SecurityContext から取得 ----
            String username = "anonymous";
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()
                    && !"anonymousUser".equals(auth.getPrincipal())) {
                username = auth.getName();
            }

            // ---- ⑤ クライアント IP アドレスを解決 ----
            String ipAddress = resolveClientIp(request);

            // ---- ⑥ 操作ログエンティティを構築 ----
            OperationLog entry = OperationLog.builder()
                    .userId(null)                              // 現時点では username のみ記録
                    .username(username)                        // ログインユーザー名
                    .operation(loggable.operation())           // @Loggable の operation 属性
                    .targetType(loggable.targetType())         // @Loggable の targetType 属性
                    .targetId(null)                            // パスパラメータからの ID 抽出は省略
                    .detail(request.getMethod() + " " + request.getRequestURI()) // HTTP メソッド + パス
                    .ipAddress(ipAddress)
                    .build();

            // 非同期でログを記録（レスポンス後なので遅延なし）
            operationLogService.record(entry);

        } catch (Exception e) {
            // ログ記録の失敗は業務処理に影響させない（ログ出力のみ）
            log.warn("操作ログ記録中にエラーが発生しました: {}", e.getMessage());
        }
    }

    /**
     * クライアントの実 IP アドレスを解決する
     *
     * <p>リバースプロキシ（nginx）経由の場合は {@code X-Forwarded-For} ヘッダーを優先する。</p>
     *
     * @param request HTTP リクエスト
     * @return クライアント IP アドレス文字列
     */
    private String resolveClientIp(HttpServletRequest request) {
        // X-Forwarded-For ヘッダーが存在すれば最初の IP（実クライアント）を使用
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
