package com.company.pcmgmt.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 認証フィルター
 *
 * <p>各リクエストに対してリクエストヘッダーの JWT トークンを検証し、
 * 有効なトークンであれば Spring Security のセキュリティコンテキストに認証情報をセットする。</p>
 *
 * <p>{@link OncePerRequestFilter} を継承することで、1リクエストにつき1回だけ実行されることを保証する。</p>
 *
 * <p>処理フロー:
 * <ol>
 *   <li>Authorization ヘッダーから "Bearer " プレフィックスを除いてトークンを抽出</li>
 *   <li>{@link JwtTokenProvider#validateToken(String)} でトークンの有効性を検証</li>
 *   <li>有効な場合はトークンからユーザー名を取得して UserDetails を読み込む</li>
 *   <li>SecurityContextHolder に認証情報をセット</li>
 * </ol>
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    /** JWT トークンの生成・検証を担うコンポーネント */
    private final JwtTokenProvider jwtTokenProvider;

    /** DBからユーザー情報を取得するサービス */
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * リクエストごとに JWT 認証処理を実行する
     *
     * @param request     HTTP リクエスト
     * @param response    HTTP レスポンス
     * @param filterChain フィルターチェーン（次のフィルターへ処理を渡す）
     * @throws ServletException サーブレット例外
     * @throws IOException      入出力例外
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Authorization ヘッダーから JWT トークンを抽出
        String token = extractTokenFromRequest(request);

        // トークンが存在し、かつ有効な場合のみ認証処理を実行
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // トークンからユーザー名を取得
            String username = jwtTokenProvider.getUsernameFromToken(token);
            // ユーザー名から UserDetails を取得（DB から）
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // Spring Security の認証トークンを生成
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            userDetails,                      // プリンシパル（認証済みユーザー情報）
                            null,                             // クレデンシャル（JWT 認証では不要）
                            userDetails.getAuthorities());   // 権限リスト（GrantedAuthority）
            // リクエスト詳細情報（IP アドレス等）を認証情報に付加
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            // セキュリティコンテキストに認証情報をセット（以降の処理で認証済みとして扱われる）
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        // 次のフィルターへ処理を渡す（認証の可否に関わらず必ず実行）
        filterChain.doFilter(request, response);
    }

    /**
     * HTTP リクエストの Authorization ヘッダーから JWT トークンを抽出する
     *
     * <p>"Bearer " プレフィックスを除いたトークン文字列を返す。
     * Authorization ヘッダーがない場合や "Bearer " で始まらない場合は null を返す。</p>
     *
     * @param request HTTP リクエスト
     * @return JWT トークン文字列（存在しない場合は null）
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        // Authorization ヘッダーを取得
        String bearerToken = request.getHeader("Authorization");
        // "Bearer " プレフィックスで始まる場合はプレフィックスを除いてトークンを返す
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " の7文字を除いた部分がトークン
        }
        return null;
    }
}
