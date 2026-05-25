package com.company.pcmgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * PC管理システム - Spring Boot アプリケーションエントリポイント
 *
 * <p>Spring Boot の自動設定・コンポーネントスキャン・Beanの登録を有効化する。
 * このクラスの {@code main} メソッドがアプリケーション起動の起点となる。</p>
 *
 * <p>{@link EnableAsync}: {@code @Async} アノテーションによる非同期処理を有効化する。
 * 操作ログの非同期記録（{@link com.company.pcmgmt.service.OperationLogService}）で使用する。</p>
 */
@SpringBootApplication
@EnableAsync
public class PcMgmtApplication {

    /**
     * アプリケーション起動メソッド
     *
     * @param args コマンドライン引数
     */
    public static void main(String[] args) {
        // Spring Boot アプリケーションを起動する
        SpringApplication.run(PcMgmtApplication.class, args);
    }
}
