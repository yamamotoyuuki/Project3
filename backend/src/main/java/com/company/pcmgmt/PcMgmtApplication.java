package com.company.pcmgmt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * PC管理システム - Spring Boot アプリケーションエントリポイント
 *
 * <p>Spring Boot の自動設定・コンポーネントスキャン・Beanの登録を有効化する。
 * このクラスの {@code main} メソッドがアプリケーション起動の起点となる。</p>
 */
@SpringBootApplication
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
