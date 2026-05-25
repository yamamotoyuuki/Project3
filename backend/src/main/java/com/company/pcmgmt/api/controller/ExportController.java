package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.annotation.Loggable;
import com.company.pcmgmt.service.ExportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * エクスポートコントローラー
 *
 * <p>PC資産・貸出データのダウンロードエンドポイントを提供する。</p>
 *
 * <p>ベースパス: {@code /api/v1/export}</p>
 *
 * <p>対応フォーマット:</p>
 * <ul>
 *   <li>CSV: UTF-8 BOM 付き（Excel で直接開いても文字化けしない）</li>
 *   <li>Excel: xlsx 形式（Apache POI 使用）</li>
 * </ul>
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/export")
@RequiredArgsConstructor
public class ExportController {

    /** エクスポート処理ビジネスロジックを担うサービス */
    private final ExportService exportService;

    /** ファイル名に使用する日付フォーマット（例: 20260525） */
    private static final DateTimeFormatter FILE_DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    // ==================================================================
    // PC 資産 エクスポート
    // ==================================================================

    /**
     * PC資産一覧を CSV でダウンロードする
     *
     * <p>エンドポイント: {@code GET /api/v1/export/assets.csv}</p>
     * <p>ファイル名例: {@code assets_20260525.csv}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @return CSV ファイルダウンロードレスポンス
     */
    @Loggable(operation = "EXPORT", targetType = "PC資産CSV")
    @GetMapping("/assets.csv")
    public ResponseEntity<byte[]> downloadAssetsCsv() {
        byte[] data = exportService.exportAssetsCsv();
        String filename = "assets_" + today() + ".csv";
        return buildDownloadResponse(data, filename, "text/csv;charset=UTF-8");
    }

    /**
     * PC資産一覧を Excel（.xlsx）でダウンロードする
     *
     * <p>エンドポイント: {@code GET /api/v1/export/assets.xlsx}</p>
     * <p>ファイル名例: {@code assets_20260525.xlsx}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @return Excel ファイルダウンロードレスポンス
     * @throws IOException Excel ファイル生成中のストリームエラー
     */
    @Loggable(operation = "EXPORT", targetType = "PC資産Excel")
    @GetMapping("/assets.xlsx")
    public ResponseEntity<byte[]> downloadAssetsExcel() throws IOException {
        byte[] data = exportService.exportAssetsExcel();
        String filename = "assets_" + today() + ".xlsx";
        return buildDownloadResponse(data, filename,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    // ==================================================================
    // 貸出 エクスポート
    // ==================================================================

    /**
     * 貸出一覧を CSV でダウンロードする
     *
     * <p>エンドポイント: {@code GET /api/v1/export/loans.csv}</p>
     * <p>ファイル名例: {@code loans_20260525.csv}</p>
     * <p>認証: JWT 認証必須</p>
     *
     * @return CSV ファイルダウンロードレスポンス
     */
    @Loggable(operation = "EXPORT", targetType = "貸出CSV")
    @GetMapping("/loans.csv")
    public ResponseEntity<byte[]> downloadLoansCsv() {
        byte[] data = exportService.exportLoansCsv();
        String filename = "loans_" + today() + ".csv";
        return buildDownloadResponse(data, filename, "text/csv;charset=UTF-8");
    }

    // ==================================================================
    // ユーティリティ
    // ==================================================================

    /**
     * ファイルダウンロード用 ResponseEntity を構築する
     *
     * <p>{@code Content-Disposition: attachment} ヘッダーを付与することで
     * ブラウザにファイルとして保存させる。</p>
     *
     * @param data        レスポンスボディのバイト配列
     * @param filename    ダウンロード時のファイル名
     * @param contentType Content-Type 文字列
     * @return ダウンロードレスポンス
     */
    private ResponseEntity<byte[]> buildDownloadResponse(byte[] data, String filename, String contentType) {
        return ResponseEntity.ok()
                // ファイルとして保存させる
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                // Content-Type を明示
                .contentType(MediaType.parseMediaType(contentType))
                // データを本文として返す
                .body(data);
    }

    /**
     * ファイル名用の今日の日付文字列を返す（例: "20260525"）
     */
    private String today() {
        return LocalDate.now().format(FILE_DATE_FMT);
    }
}
