package com.company.pcmgmt.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 共通 API レスポンスラッパー DTO
 *
 * <p>すべての REST API レスポンスをこのクラスでラップして返す。
 * {@code code}（結果コード）・{@code message}（メッセージ）・{@code data}（ペイロード）
 * の統一フォーマットでクライアントに応答する。</p>
 *
 * <p>{@code null} フィールドは JSON 出力から除外される（{@code @JsonInclude(NON_NULL)}）。</p>
 *
 * @param <T> レスポンスデータの型
 */
@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /** 処理結果コード（例: "SUCCESS", "NOT_FOUND", "VALIDATION_ERROR"） */
    private String code;

    /** 処理結果メッセージ（例: "処理が完了しました", "リソースが見つかりません"） */
    private String message;

    /** レスポンスデータ本体（型パラメータ T に応じた任意のオブジェクト） */
    private T data;

    /** レスポンス生成日時 */
    private LocalDateTime timestamp;

    /**
     * 成功レスポンスを生成する（メッセージ省略版）
     *
     * @param data    レスポンスデータ
     * @param <T>     データ型
     * @return SUCCESS コードの ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")                  // 成功コード固定
                .message("処理が完了しました")      // デフォルトメッセージ
                .data(data)
                .timestamp(LocalDateTime.now())   // 現在日時をタイムスタンプに設定
                .build();
    }

    /**
     * 成功レスポンスを生成する（メッセージ指定版）
     *
     * @param message カスタムメッセージ
     * @param data    レスポンスデータ
     * @param <T>     データ型
     * @return SUCCESS コードの ApiResponse
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code("SUCCESS")
                .message(message)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }

    /**
     * エラーレスポンスを生成する
     *
     * @param code    エラーコード（例: "NOT_FOUND", "VALIDATION_ERROR", "INTERNAL_ERROR"）
     * @param message エラーメッセージ
     * @param <T>     データ型（エラー時は通常 Void）
     * @return エラーコードの ApiResponse（data は null）
     */
    public static <T> ApiResponse<T> error(String code, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .message(message)
                .timestamp(LocalDateTime.now())   // エラー発生日時
                .build();
    }
}
