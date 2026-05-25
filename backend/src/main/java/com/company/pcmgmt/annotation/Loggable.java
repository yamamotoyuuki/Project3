package com.company.pcmgmt.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作ログ記録アノテーション
 *
 * <p>このアノテーションを付与したコントローラーメソッドが呼び出された際に、
 * {@code OperationLoggingAspect} が自動的に {@code operation_logs} テーブルへ
 * 操作ログを書き込む。</p>
 *
 * <p>使用例:</p>
 * <pre>{@code
 * @Loggable(operation = "CREATE", targetType = "PC資産")
 * public ResponseEntity<?> create(@RequestBody AssetCreateRequest req) { ... }
 * }</pre>
 */
@Target(ElementType.METHOD)          // メソッドにのみ付与可能
@Retention(RetentionPolicy.RUNTIME)  // 実行時にリフレクションで参照可能
public @interface Loggable {

    /**
     * 操作種別（例: "CREATE", "UPDATE", "DELETE", "RETURN"）
     * operation_logs.operation カラムに書き込まれる。
     */
    String operation();

    /**
     * 対象リソース種別（例: "PC資産", "社員", "貸出"）
     * operation_logs.target_type カラムに書き込まれる。
     */
    String targetType() default "";
}
