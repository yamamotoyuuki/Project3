package com.company.pcmgmt.config;

import com.company.pcmgmt.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 設定クラス
 *
 * <p>JWT ベースのステートレス認証・アクセス制御・CORS 設定を行う。
 * セッションは使用せず（STATELESS）、各リクエストで JWT トークンを検証する。</p>
 *
 * <p>{@code @EnableMethodSecurity} により {@code @PreAuthorize} アノテーションが有効になる。</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity    // @PreAuthorize によるメソッドレベルのアクセス制御を有効化
@RequiredArgsConstructor
public class SecurityConfig {

    /** JWT 認証フィルター（各リクエストでトークン検証を行う） */
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * セキュリティフィルターチェーンを設定する
     *
     * <p>設定内容:
     * <ul>
     *   <li>CSRF 無効化（REST API のため不要）</li>
     *   <li>CORS 設定の適用</li>
     *   <li>セッション管理: STATELESS（JWT 認証のためセッション不使用）</li>
     *   <li>エンドポイントアクセス制御の設定</li>
     *   <li>JWT フィルターをフィルターチェーンに追加</li>
     * </ul>
     * </p>
     *
     * @param http HttpSecurity ビルダー
     * @return 設定済み SecurityFilterChain
     * @throws Exception 設定エラー
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // CSRF 保護を無効化（REST API では Cookie を使わないため不要）
            .csrf(AbstractHttpConfigurer::disable)
            // CORS 設定を適用
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // セッションを使用しない（JWT はステートレス認証）
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // ---- 認証不要エンドポイント ----
                // ログイン・ログアウト
                .requestMatchers("/api/v1/auth/**").permitAll()
                // エージェントからの情報収集（認証なしで自動実行）
                .requestMatchers("/api/v1/agent/report").permitAll()
                // エージェント初回登録（認証なしで自動実行）
                .requestMatchers("/api/v1/agent/register").permitAll()
                // エージェント起動時の資産情報取得（購入/レンタル区分確認用、認証なしで自動実行）
                .requestMatchers("/api/v1/agent/asset-info").permitAll()
                // ヘルスチェック（監視ツール等からの疎通確認）
                .requestMatchers("/actuator/health").permitAll()
                // ---- 管理者のみアクセス可能 ----
                // ユーザー管理は ADMIN ロール必須
                .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                // ---- その他は認証必須 ----
                .anyRequest().authenticated()
            )
            // ---- 例外ハンドリング設定 ----
            .exceptionHandling(ex -> ex
                // 未認証リクエスト（JWTなし/期限切れ）には 401 Unauthorized を返す
                // ※ 設定しない場合 Spring Security は 403 を返すため明示的に設定する
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(
                        "{\"success\":false,\"message\":\"認証が必要です。ログインしてください。\"}"
                    );
                })
            )
            // JWT フィルターを UsernamePasswordAuthenticationFilter の前に挿入
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * パスワードエンコーダーを Bean 登録する
     *
     * <p>BCrypt アルゴリズムを使用する。パスワード登録時のハッシュ化と
     * ログイン時の照合に使用される。</p>
     *
     * @return BCryptPasswordEncoder インスタンス
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 認証マネージャーを Bean 登録する
     *
     * <p>Spring Security の認証処理の中心となるコンポーネント。
     * 設定から自動取得する。</p>
     *
     * @param authenticationConfiguration 認証設定
     * @return AuthenticationManager
     * @throws Exception 設定エラー
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * CORS（クロスオリジンリソース共有）設定を作成する
     *
     * <p>フロントエンド（Vue.js / Tauri）からのリクエストを許可するための設定。
     * 許可オリジン: localhost の全ポート・任意の HTTPS オリジン</p>
     *
     * @return CORS 設定ソース
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // 許可するオリジン（localhost 全ポートおよび HTTPS ドメイン）
        configuration.setAllowedOriginPatterns(List.of("http://localhost:*", "https://*"));
        // 許可する HTTP メソッド
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        // 許可するリクエストヘッダー（全て許可）
        configuration.setAllowedHeaders(List.of("*"));
        // Cookie・認証情報を含むリクエストを許可（JWT の Authorization ヘッダーに必要）
        configuration.setAllowCredentials(true);

        // /api/** パス以下に CORS 設定を適用
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
