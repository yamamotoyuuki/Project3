package com.company.pcmgmt.exception;

/**
 * リソース未検出例外
 *
 * <p>指定した ID のリソースがデータベースに存在しない場合にスローする。
 * {@link GlobalExceptionHandler} でキャッチされ、HTTP 404 Not Found として返却される。</p>
 */
public class ResourceNotFoundException extends RuntimeException {

    /**
     * メッセージを指定して例外を生成する
     *
     * @param message エラーメッセージ
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * リソース名と ID を指定して例外を生成する
     *
     * <p>メッセージは "{リソース名} が見つかりません。ID: {id}" の形式で自動生成される。</p>
     *
     * @param resource リソース名（例: "PC資産", "社員"）
     * @param id       対象リソースの ID
     */
    public ResourceNotFoundException(String resource, Long id) {
        super(resource + " が見つかりません。ID: " + id);
    }
}
