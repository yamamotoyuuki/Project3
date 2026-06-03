package com.company.pcmgmt.service.export;

import com.company.pcmgmt.api.dto.request.asset.AssetSearchRequest;
import com.company.pcmgmt.api.dto.request.loan.LoanSearchRequest;
import com.company.pcmgmt.api.dto.response.asset.AssetResponse;
import com.company.pcmgmt.api.dto.response.loan.LoanResponse;
import com.company.pcmgmt.domain.mapper.loan.LoanMapper;
import com.company.pcmgmt.domain.mapper.asset.PcAssetMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * エクスポートサービス
 *
 * <p>PC資産・貸出データを CSV および Excel（.xlsx）形式でエクスポートする。</p>
 *
 * <p>使用ライブラリ:</p>
 * <ul>
 *   <li>CSV: 標準 {@link PrintWriter}（文字コード UTF-8 BOM 付き）</li>
 *   <li>Excel: Apache POI {@link XSSFWorkbook}（xlsx 形式）</li>
 * </ul>
 *
 * <p>全件取得のため、大量データの場合はメモリ使用量に注意すること。
 * 将来的にはストリーミングエクスポートへの移行を検討する。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    /** PC資産の DB アクセスを担うマッパー */
    private final PcAssetMapper pcAssetMapper;

    /** 貸出の DB アクセスを担うマッパー */
    private final LoanMapper loanMapper;

    /** 日付フォーマット（CSV/Excel 出力用） */
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");

    // ==================================================================
    // PC 資産 エクスポート
    // ==================================================================

    /**
     * PC資産一覧を CSV 形式でエクスポートする
     *
     * <p>文字コードは UTF-8 BOM 付き（Excel で直接開いた際の文字化け防止）。</p>
     *
     * @return CSV バイト配列
     */
    @Transactional(readOnly = true)
    public byte[] exportAssetsCsv() {
        // 全件取得（検索条件なし・ページング無効）
        AssetSearchRequest req = buildAllRequest();
        List<AssetResponse> assets = pcAssetMapper.findAll(req);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // UTF-8 BOM を書き込む（Excel が文字化けしないよう先頭に付与）
        baos.write(0xEF);
        baos.write(0xBB);
        baos.write(0xBF);

        try (PrintWriter writer = new PrintWriter(baos, true, java.nio.charset.StandardCharsets.UTF_8)) {
            // ヘッダー行
            writer.println("資産番号,端末名,取得区分,メーカー,型番,シリアル番号,設置場所,ステータス,担当社員,ホスト名,備考,登録日時");

            // データ行
            for (AssetResponse a : assets) {
                writer.println(String.join(",",
                        escapeCsv(a.getAssetNumber()),
                        escapeCsv(a.getDeviceName()),
                        escapeCsv(translateAcquisitionType(a.getAcquisitionType())),
                        escapeCsv(a.getMaker()),
                        escapeCsv(a.getModelNumber()),
                        escapeCsv(a.getSerialNumber()),
                        escapeCsv(a.getLocation()),
                        escapeCsv(translateStatus(a.getStatus())),
                        escapeCsv(a.getAssignedEmployeeName()),
                        escapeCsv(a.getHostname()),
                        escapeCsv(a.getNote()),
                        a.getCreatedAt() != null ? a.getCreatedAt().format(DATETIME_FMT) : ""
                ));
            }
        }
        return baos.toByteArray();
    }

    /**
     * PC資産一覧を Excel（.xlsx）形式でエクスポートする
     *
     * @return Excel バイト配列
     * @throws IOException Excel 生成時のストリームエラー
     */
    @Transactional(readOnly = true)
    public byte[] exportAssetsExcel() throws IOException {
        AssetSearchRequest req = buildAllRequest();
        List<AssetResponse> assets = pcAssetMapper.findAll(req);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("PC資産一覧");

            // ---- ヘッダースタイル（太字・背景色付き） ----
            CellStyle headerStyle = createHeaderStyle(workbook);

            // ---- ヘッダー行（行番号 0） ----
            String[] headers = {"資産番号", "端末名", "取得区分", "メーカー", "型番",
                    "シリアル番号", "設置場所", "ステータス", "担当社員", "ホスト名", "備考", "登録日時"};
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ---- データ行（行番号 1 以降） ----
            int rowNum = 1;
            for (AssetResponse a : assets) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(nullToEmpty(a.getAssetNumber()));
                row.createCell(1).setCellValue(nullToEmpty(a.getDeviceName()));
                row.createCell(2).setCellValue(translateAcquisitionType(a.getAcquisitionType()));
                row.createCell(3).setCellValue(nullToEmpty(a.getMaker()));
                row.createCell(4).setCellValue(nullToEmpty(a.getModelNumber()));
                row.createCell(5).setCellValue(nullToEmpty(a.getSerialNumber()));
                row.createCell(6).setCellValue(nullToEmpty(a.getLocation()));
                row.createCell(7).setCellValue(translateStatus(a.getStatus()));
                row.createCell(8).setCellValue(nullToEmpty(a.getAssignedEmployeeName()));
                row.createCell(9).setCellValue(nullToEmpty(a.getHostname()));
                row.createCell(10).setCellValue(nullToEmpty(a.getNote()));
                row.createCell(11).setCellValue(
                        a.getCreatedAt() != null ? a.getCreatedAt().format(DATETIME_FMT) : "");
            }

            // 全列の幅を自動調整
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(baos);
            return baos.toByteArray();
        }
    }

    // ==================================================================
    // 貸出 エクスポート
    // ==================================================================

    /**
     * 貸出一覧を CSV 形式でエクスポートする（UTF-8 BOM 付き）
     *
     * @return CSV バイト配列
     */
    @Transactional(readOnly = true)
    public byte[] exportLoansCsv() {
        // 全件取得（未返却含む全貸出）
        LoanSearchRequest req = new LoanSearchRequest();
        req.setPage(0);
        req.setSize(Integer.MAX_VALUE);
        List<LoanResponse> loans = loanMapper.findAll(req);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        // UTF-8 BOM
        baos.write(0xEF);
        baos.write(0xBB);
        baos.write(0xBF);

        try (PrintWriter writer = new PrintWriter(baos, true, java.nio.charset.StandardCharsets.UTF_8)) {
            // ヘッダー行
            writer.println("貸出ID,資産番号,端末名,社員番号,社員名,貸出日,返却予定日,実返却日,目的,状態,備考");

            // データ行
            for (LoanResponse l : loans) {
                writer.println(String.join(",",
                        String.valueOf(l.getId()),
                        escapeCsv(l.getAssetNumber()),
                        escapeCsv(l.getDeviceName()),
                        escapeCsv(l.getEmployeeCode()),
                        escapeCsv(l.getEmployeeName()),
                        l.getLoanDate() != null ? l.getLoanDate().format(DATE_FMT) : "",
                        l.getExpectedReturnDate() != null ? l.getExpectedReturnDate().format(DATE_FMT) : "",
                        l.getActualReturnDate() != null ? l.getActualReturnDate().format(DATE_FMT) : "",
                        escapeCsv(l.getPurpose()),
                        l.isReturned() ? "返却済" : (l.isOverdue() ? "延滞中" : "貸出中"),
                        escapeCsv(l.getNote())
                ));
            }
        }
        return baos.toByteArray();
    }

    // ==================================================================
    // ユーティリティ
    // ==================================================================

    /**
     * 全件取得用の検索リクエストを生成する
     * ページング無効化のため size に Integer.MAX_VALUE を設定する。
     */
    private AssetSearchRequest buildAllRequest() {
        AssetSearchRequest req = new AssetSearchRequest();
        req.setPage(0);
        req.setSize(Integer.MAX_VALUE);
        return req;
    }

    /**
     * Excel ヘッダーセルのスタイルを生成する（太字・薄い青背景）
     *
     * @param workbook 対象ワークブック
     * @return ヘッダー用 CellStyle
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        // 薄い青背景（#BDD7EE に近いインデックスカラー）
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        // ボーダー
        style.setBorderBottom(BorderStyle.THIN);
        return style;
    }

    /**
     * CSV 用のエスケープ処理
     * カンマ・ダブルクォート・改行を含むフィールドはダブルクォートで囲む。
     *
     * @param value エスケープ対象の文字列（null の場合は空文字）
     * @return CSV フィールド文字列
     */
    private String escapeCsv(String value) {
        if (value == null) return "";
        // ダブルクォートを二重化してからフィールド全体をクォートで囲む
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    /**
     * null を空文字に変換するユーティリティ
     *
     * @param value 変換対象
     * @return null の場合は ""、それ以外はそのまま返す
     */
    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    /**
     * 取得区分のコードを日本語ラベルに変換する
     *
     * @param code 取得区分コード（"PURCHASE" / "RENTAL"）
     * @return 日本語ラベル
     */
    private String translateAcquisitionType(String code) {
        if (code == null) return "";
        return switch (code) {
            case "PURCHASE" -> "購入";
            case "RENTAL"   -> "レンタル";
            default         -> code;
        };
    }

    /**
     * ステータスコードを日本語ラベルに変換する
     *
     * @param code ステータスコード（"IN_USE" 等）
     * @return 日本語ラベル
     */
    private String translateStatus(String code) {
        if (code == null) return "";
        return switch (code) {
            case "IN_USE"     -> "使用中";
            case "IN_STORAGE" -> "保管中";
            case "DISPOSED"   -> "廃棄済";
            case "IN_REPAIR"  -> "修理中";
            case "RETURNED"   -> "返却済";
            default           -> code;
        };
    }
}
