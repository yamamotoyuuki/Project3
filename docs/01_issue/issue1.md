# 問題
下記に示す、情報１と情報２の構造が一致していない。
情報１に合わせて、情報２の構造を修正してください。
また、情報１のapiUrlの項目は不要です。

# 情報１：agentアプリで、送信ボタンを押下したときのJSON形式（値はテスト値）
{
  "apiUrl": "http://localhost:8080/api/v1",
  "report": {
    "asset_number": "PC-TEST01",
    "location": "テスト設置場所",
    "hostname": "LAPTOP-3N4AQL6D",
    "hardware": {
      "cpu_model": "13th Gen Intel(R) Core(TM) i7-13700HX",
      "cpu_cores": 24,
      "memory_gb": 13.7,
      "disk_gb": 952.6,
      "disk_free_gb": 260.8
    },
    "os": {
      "name": "Windows",
      "version": "11 (26200)"
    },
    "network": [
      {
        "ip": "192.168.0.93",
        "mac": "N/A"
      }
    ],
    "software": [
      {
        "name": "Git",
        "version": "2.48.1"
      },
      {
        "name": "HP Documentation",
        "version": "1.0.0.1"
      },
     ]
   }
}

# 情報２：agetnアプリのリクエストを受けるサーバ処理のDto（AgentReportRequest.java）
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
