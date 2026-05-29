package com.company.pcmgmt.api.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

/**
 * エージェント情報報告リクエスト DTO
 *
 * <p>POST /api/v1/agent/report のリクエストボディにマッピングされる。
 * Tauri エージェント（クライアントPC上で動作するデスクトップアプリ）が
 * sysinfo クレートで収集した情報をこの DTO で受け取る。</p>
 *
 * <p>JSON 構造はエージェント側（Rust/AgentReport）と一致させること。
 * ハードウェア・OS・ネットワーク情報はそれぞれネストされたオブジェクトとして受信する。</p>
 *
 * <p>このエンドポイントは JWT 認証不要（SecurityConfig で permitAll 設定済み）。</p>
 */
@Data
public class AgentReportRequest {

    /**
     * エージェント番号（エージェントアプリが保持する端末固有の識別子）
     * 初回起動時に /api/v1/agent/register で取得してローカルに保存した値。
     * JSON キーは snake_case のため @JsonProperty でマッピングする。
     */
    @JsonProperty("agent_number")
    private String agentNumber;

    /**
     * 取得区分（エージェント設定画面でユーザーが選択した場合のみ送信）
     * <ul>
     *   <li>RENTAL: レンタル品として登録する（エージェントが選択した場合のみ送信）</li>
     *   <li>null: 送信しない（バックエンドから取得済み、または未選択）</li>
     * </ul>
     * JSON キーは snake_case のため @JsonProperty でマッピングする。
     */
    @JsonProperty("acquisition_type")
    private String acquisitionType;

    /**
     * PC の資産番号（エージェント設定画面でユーザーが入力）
     * JSON キーは snake_case のため @JsonProperty でマッピングする。
     */
    @JsonProperty("asset_number")
    private String assetNumber;

    /** 設置場所（エージェント設定画面でユーザーが入力。pc_assets.location に登録される） */
    private String location;

    /**
     * 使用者名（エージェント設定画面でユーザーが入力）
     * バックエンドで employees.full_name と照合し、
     * 一致する社員の ID を pc_assets.assigned_employee_id に登録する。
     * JSON キーは snake_case のため @JsonProperty でマッピングする。
     */
    @JsonProperty("user_name")
    private String userName;

    /** エージェントが動作しているPCのホスト名（PC資産との紐付けキー） */
    private String hostname;

    /** ハードウェア情報（CPU・メモリ・ディスク） */
    private Hardware hardware;

    /** OS 情報（名称・バージョン） */
    private Os os;

    /** ネットワークインターフェース情報のリスト（複数NIC対応） */
    private List<Network> network;

    /** インストール済みソフトウェアのリスト */
    private List<Software> software;

    // =====================================================
    // 内部 DTO クラス
    // =====================================================

    /**
     * ハードウェア情報（内部 DTO）
     *
     * <p>CPU・メモリ・ディスクの情報を保持する。
     * JSON キーは snake_case のため各フィールドに @JsonProperty を付与する。</p>
     */
    @Data
    public static class Hardware {

        /** CPU モデル名（例: "13th Gen Intel(R) Core(TM) i7-13700HX"） */
        @JsonProperty("cpu_model")
        private String cpuModel;

        /** CPU 論理コア数（ハイパースレッディング含む） */
        @JsonProperty("cpu_cores")
        private Integer cpuCores;

        /** 搭載メモリ容量（GB単位、小数点第1位まで） */
        @JsonProperty("memory_gb")
        private BigDecimal memoryGb;

        /** ディスク総容量（全ドライブ合計、GB単位） */
        @JsonProperty("disk_gb")
        private BigDecimal diskGb;

        /** ディスク空き容量（全ドライブ合計、GB単位） */
        @JsonProperty("disk_free_gb")
        private BigDecimal diskFreeGb;
    }

    /**
     * OS 情報（内部 DTO）
     *
     * <p>OS の名称とバージョンを保持する。</p>
     */
    @Data
    public static class Os {

        /** OS 名称（例: "Windows", "macOS"） */
        private String name;

        /** OS バージョン（例: "11 (26200)", "14.0"） */
        private String version;
    }

    /**
     * ネットワークインターフェース情報（内部 DTO）
     *
     * <p>PC の各 NIC（ネットワークアダプタ）の IP・MAC アドレスを保持する。</p>
     */
    @Data
    public static class Network {

        /** 割り当てられた IP アドレス（例: "192.168.0.93"） */
        private String ip;

        /** MAC アドレス（例: "AA:BB:CC:DD:EE:FF"、取得できない場合は "N/A"） */
        private String mac;
    }

    /**
     * インストール済みソフトウェア情報（内部 DTO）
     *
     * <p>PC にインストールされているアプリケーションの名称とバージョンを保持する。
     * ソフトウェアマスタとの照合によりライセンス超過検出に使用される。</p>
     */
    @Data
    public static class Software {

        /** ソフトウェア名（例: "Git", "Microsoft Office 2021"） */
        private String name;

        /** ソフトウェアのバージョン（例: "2.48.1", "16.0.14701"） */
        private String version;
    }
}
