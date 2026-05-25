package com.company.pcmgmt.config;

import com.company.pcmgmt.interceptor.OperationLoggingInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 設定クラス
 *
 * <p>インターセプターの登録など、Spring MVC のカスタム設定を行う。</p>
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * 操作ログを記録するインターセプター
     * {@link com.company.pcmgmt.annotation.Loggable} 付きコントローラーメソッドの
     * 正常終了後に操作ログを記録する。
     */
    private final OperationLoggingInterceptor operationLoggingInterceptor;

    /**
     * インターセプターを登録する
     *
     * <p>全パス（/**）に適用し、/api/** の全エンドポイントでログが記録されるようにする。</p>
     *
     * @param registry インターセプターレジストリ
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(operationLoggingInterceptor)
                .addPathPatterns("/api/**"); // API エンドポイントのみに適用
    }
}
