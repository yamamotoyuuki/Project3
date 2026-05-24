package com.company.pcmgmt.api.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * エージェント情報報告リクエスト DTO
 *
 * <p>POST /api/v1/agent/report のリクエストボディにマッピングされる。
 * Tauri エージェント（クライアントPC上で動作するデスクトップアプリ）が
 * sysinfo クレートで収集した情報をこの DTO で送信する。</p>
 *
 * <p>このエンドポイントは JWT 認証不要（SecurityConfig で permitAll 設定済み）。</p>
 */
@Data
public class AgentReportRequest {

    /** エージェントが動作しているPCのホスト名（PC資産との紐付けキー） */
    private String hostname;

    /** CPU モデル名（例: "Intel Core i7-1365U"） */
    private String cpuModel;

    /** CPU 物理コア数 */
    private Integer cpuCores;

    /** 搭載メモリ容量（GB単位） */
    private BigDecimal memoryGb;

    /** ディスク総容量（GB単位） */
    private BigDecimal diskGb;

    /** ディスク空き容量（GB単位） */
    private BigDecimal diskFreeGb;

    /** OS名（例: "Windows 11 Home"） */
    private String osName;

    /** OSバージョン（例: "10.0.22621"） */
    private String osVersion;

    /** ネットワークインターフェース情報のリスト */
    private List<NetworkInterface> networkInterfaces;

    /** インストール済みソフトウェアのリスト */
    private List<InstalledSoftware> installedSoftware;

    /**
     * ネットワークインターフェース情報（内部 DTO）
     *
     * <p>PC の各 NIC（ネットワークアダプタ）の情報を保持する。</p>
     */
    @Data
    public static class NetworkInterface {

        /** NIC（ネットワークインターフェースカード）の名称（例: "Ethernet", "Wi-Fi"） */
        private String nicName;

        /** 割り当てられた IP アドレス（例: "192.168.1.10"） */
        private String ipAddress;

        /** MAC アドレス（例: "AA:BB:CC:DD:EE:FF"） */
        private String macAddress;
    }

    /**
     * インストール済みソフトウェア情報（内部 DTO）
     *
     * <p>PC にインストールされているアプリケーションの情報を保持する。
     * ソフトウェアマスタとの照合によりライセンス超過検出に使用される。</p>
     */
    @Data
    public static class InstalledSoftware {

        /** ソフトウェア名（例: "Microsoft Office 2021"） */
        private String softwareName;

        /** ソフトウェアのバージョン（例: "16.0.14701"） */
        private String version;

        /** 発行元・メーカー名（例: "Microsoft Corporation"） */
        private String publisher;

        /** インストール日（例: "2024-01-15"、形式はエージェント依存） */
        private String installDate;
    }
}
