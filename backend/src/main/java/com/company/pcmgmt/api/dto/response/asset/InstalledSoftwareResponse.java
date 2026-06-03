package com.company.pcmgmt.api.dto.response.asset;

import lombok.Data;

/**
 * インストール済みソフトウェア レスポンス DTO
 *
 * <p>GET /api/v1/assets/{id}/software が返却するデータ構造。
 * pc_software_info テーブルの1件分に対応する。</p>
 */
@Data
public class InstalledSoftwareResponse {

    /** レコードID（主キー） */
    private Long id;

    /** ソフトウェア名（例: "Microsoft Edge", "Git"） */
    private String softwareName;

    /** バージョン文字列（例: "120.0.2210.91"）、未取得の場合は null */
    private String version;

    /** 発行元・パブリッシャー名（例: "Microsoft Corporation"）、未取得の場合は null */
    private String publisher;
}
