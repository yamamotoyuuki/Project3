/**
 * lib.rs
 * -----------------------------------------------
 * PC管理エージェント - コアロジック
 *
 * Tauri コマンドとして以下の機能を提供する:
 *   - collect_pc_info: sysinfo クレートでPC情報を収集
 *   - send_report    : 収集情報をバックエンド API へ POST 送信
 *
 * 収集情報:
 *   - CPU（モデル名・コア数）
 *   - メモリ（総量 GB）
 *   - ディスク（合計・空き GB）
 *   - OS（名称・バージョン）
 *   - ネットワーク（ローカル IP）
 *   - インストール済みソフトウェア（OS ごとのコマンドで取得）
 * -----------------------------------------------
 */
use serde::{Deserialize, Serialize};
use sysinfo::{Disks, System};

// =====================================================
// データ構造（AgentReport とその内部型）
// =====================================================

/// CPU・メモリ・ディスク情報を保持する構造体
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct HardwareInfo {
    /// CPU モデル名（例: "Intel(R) Core(TM) i7-1165G7 @ 2.80GHz"）
    pub cpu_model: String,
    /// CPU 論理コア数
    pub cpu_cores: usize,
    /// 総メモリ量（GB、小数点第1位まで）
    pub memory_gb: f64,
    /// ディスク総容量（GB、全ドライブ合計）
    pub disk_gb: f64,
    /// ディスク空き容量（GB、全ドライブ合計）
    pub disk_free_gb: f64,
}

/// OS 情報を保持する構造体
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct OsInfo {
    /// OS 名称（例: "Windows", "macOS"）
    pub name: String,
    /// OS バージョン（例: "11", "14.0"）
    pub version: String,
}

/// ネットワークインターフェース情報を保持する構造体
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct NetworkInfo {
    /// ローカル IP アドレス（例: "192.168.1.10"）
    pub ip: String,
    /// MAC アドレス（現バージョンでは取得を省略し "N/A" を設定）
    pub mac: String,
}

/// インストール済みソフトウェア情報を保持する構造体
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct SoftwareInfo {
    /// ソフトウェア名
    pub name: String,
    /// バージョン文字列（取得できない場合は空文字）
    pub version: String,
}

/// バックエンドに送信するレポート全体を表す構造体
#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct AgentReport {
    /// PC の資産番号（ユーザーが設定画面で入力）
    pub asset_number: String,
    /// 設置場所（ユーザーが設定画面で入力）
    pub location: String,
    /// PC のホスト名（OS から自動取得）
    pub hostname: String,
    /// ハードウェア情報
    pub hardware: HardwareInfo,
    /// OS 情報
    pub os: OsInfo,
    /// ネットワーク情報の一覧（複数 NIC に対応）
    pub network: Vec<NetworkInfo>,
    /// インストール済みソフトウェア一覧
    pub software: Vec<SoftwareInfo>,
    /// 情報収集日時（ISO 8601 形式）
    pub collected_at: String,
}

// =====================================================
// Tauri コマンド: PC 情報を収集する
// =====================================================

/// PC のハードウェア・OS・ネットワーク・ソフトウェア情報を収集して返す。
///
/// # Returns
/// 収集成功時は `Ok(AgentReport)`、失敗時は `Err(エラーメッセージ)` を返す。
#[tauri::command]
fn collect_pc_info() -> Result<AgentReport, String> {
    // System オブジェクトを生成して全情報をリフレッシュ
    let mut sys = System::new_all();
    sys.refresh_all();

    // ---- ホスト名 ----
    let hostname = hostname::get()
        .map(|h| h.to_string_lossy().to_string())
        .unwrap_or_else(|_| "unknown".to_string());

    // ---- CPU 情報 ----
    // 最初の CPU から代表的なモデル名を取得する
    let cpu_model = sys
        .cpus()
        .first()
        .map(|c| c.brand().to_string())
        .unwrap_or_else(|| "Unknown CPU".to_string());
    // 論理コア数（ハイパースレッディング含む）
    let cpu_cores = sys.cpus().len();

    // ---- メモリ情報 ----
    // バイト単位の値を GB に変換（小数点第1位で丸め）
    let memory_gb = round1(sys.total_memory() as f64 / 1_073_741_824.0);

    // ---- ディスク情報 ----
    // sysinfo 0.30 以降は Disks::new_with_refreshed_list() を使用する
    let (disk_total_gb, disk_free_gb) = collect_disk_info();

    // ---- OS 情報 ----
    let os_name    = System::name().unwrap_or_else(|| "Unknown".to_string());
    let os_version = System::os_version().unwrap_or_else(|| "Unknown".to_string());

    // ---- ネットワーク情報 ----
    let network = collect_network_info();

    // ---- インストール済みソフトウェア ----
    let software = collect_software_info();

    // ---- 収集日時（ISO 8601 形式） ----
    let collected_at = chrono::Local::now().to_rfc3339();

    // AgentReport を組み立てて返す
    let report = AgentReport {
        asset_number: String::new(), // 設定画面でユーザーが入力する値
        location:     String::new(), // 設定画面でユーザーが入力する値
        hostname,
        hardware: HardwareInfo {
            cpu_model,
            cpu_cores,
            memory_gb,
            disk_gb: disk_total_gb,
            disk_free_gb,
        },
        os: OsInfo {
            name:    os_name,
            version: os_version,
        },
        network,
        software,
        collected_at,
    };

    Ok(report)
}

// =====================================================
// Tauri コマンド: API へレポートを送信する
// =====================================================

/// 収集したレポートをバックエンド API（POST /api/v1/agent/report）へ送信する。
///
/// # Arguments
/// * `api_url` - バックエンドの API ベース URL（例: "http://localhost:8080/api/v1"）
/// * `report`  - 送信する AgentReport
///
/// # Returns
/// 送信成功時は `Ok("送信成功")`、失敗時は `Err(エラーメッセージ)` を返す。
#[tauri::command]
async fn send_report(api_url: String, report: AgentReport) -> Result<String, String> {
    let client = reqwest::Client::new();
    // URL を組み立てる（末尾スラッシュを除去してから追記）
    let url = format!("{}/agent/report", api_url.trim_end_matches('/'));

    let response = client
        .post(&url)
        .json(&report)                         // AgentReport を JSON シリアライズして送信
        .send()
        .await
        .map_err(|e| format!("送信エラー: {}", e))?;

    if response.status().is_success() {
        Ok("送信成功".to_string())
    } else {
        Err(format!("サーバーエラー: {}", response.status()))
    }
}

// =====================================================
// 内部ユーティリティ関数
// =====================================================

/// ディスク情報を収集して（総容量 GB, 空き容量 GB）のタプルを返す。
///
/// 全ドライブ（物理ディスク）の合計値を返す。
fn collect_disk_info() -> (f64, f64) {
    // sysinfo 0.30 の API: Disks::new_with_refreshed_list() で即時リフレッシュ
    let disks = Disks::new_with_refreshed_list();

    let (total, free) = disks.list().iter().fold((0u64, 0u64), |acc, disk| {
        (
            acc.0 + disk.total_space(),     // 総容量（バイト）
            acc.1 + disk.available_space(), // 空き容量（バイト）
        )
    });

    // バイト → GB に変換（1 GB = 1,073,741,824 バイト）
    (
        round1(total as f64 / 1_073_741_824.0),
        round1(free  as f64 / 1_073_741_824.0),
    )
}

/// ローカル IP アドレスを収集して NetworkInfo の Vec を返す。
///
/// UDP ソケットを使って Google の DNS（8.8.8.8）への接続を「試みる」ことで
/// ルーティングに使われるローカル IP を取得する（実際にパケットは送信されない）。
fn collect_network_info() -> Vec<NetworkInfo> {
    let ip = std::net::UdpSocket::bind("0.0.0.0:0")
        .ok()
        .and_then(|socket| {
            // 接続先にパケットは送信されない（ルーティングテーブルの参照のみ）
            socket.connect("8.8.8.8:80").ok()?;
            socket.local_addr().ok()
        })
        .map(|addr| addr.ip().to_string())
        .unwrap_or_else(|| "127.0.0.1".to_string());

    vec![NetworkInfo {
        ip,
        mac: "N/A".to_string(), // MAC アドレスは現バージョンでは取得しない
    }]
}

/// OS ごとのシステムコマンドでインストール済みソフトウェア一覧を取得する。
///
/// | OS      | コマンド                              |
/// |---------|--------------------------------------|
/// | Windows | `wmic product get name,version`      |
/// | macOS   | `system_profiler SPApplicationsDataType -json` |
/// | Linux   | `dpkg --list` または `rpm -qa`        |
///
/// コマンドの実行に失敗した場合や解析エラーが発生した場合は空 Vec を返す。
fn collect_software_info() -> Vec<SoftwareInfo> {
    #[cfg(target_os = "windows")]
    {
        collect_software_windows()
    }
    #[cfg(target_os = "macos")]
    {
        collect_software_macos()
    }
    #[cfg(not(any(target_os = "windows", target_os = "macos")))]
    {
        collect_software_linux()
    }
}

/// Windows: wmic コマンドでインストール済みソフトウェアを取得する
#[cfg(target_os = "windows")]
fn collect_software_windows() -> Vec<SoftwareInfo> {
    // PowerShell 経由でインストール済みプログラム情報を取得（wmic は非推奨のため Get-Package を使用）
    let output = std::process::Command::new("powershell")
        .args([
            "-NoProfile",
            "-NonInteractive",
            "-Command",
            "Get-Package | Select-Object Name,Version | ConvertTo-Csv -NoTypeInformation",
        ])
        .output();

    match output {
        Ok(out) if out.status.success() => {
            let text = String::from_utf8_lossy(&out.stdout);
            parse_csv_software(&text)
        }
        _ => vec![], // コマンド失敗時は空リストを返す
    }
}

/// macOS: system_profiler JSON 出力からアプリケーション一覧を取得する
#[cfg(target_os = "macos")]
fn collect_software_macos() -> Vec<SoftwareInfo> {
    let output = std::process::Command::new("system_profiler")
        .args(["SPApplicationsDataType", "-json"])
        .output();

    match output {
        Ok(out) if out.status.success() => {
            let text = String::from_utf8_lossy(&out.stdout);
            // JSON パース: {"SPApplicationsDataType": [{"_name": "...", "version": "..."}]}
            serde_json::from_str::<serde_json::Value>(&text)
                .ok()
                .and_then(|v| v["SPApplicationsDataType"].as_array().cloned())
                .unwrap_or_default()
                .into_iter()
                .filter_map(|app| {
                    let name    = app["_name"].as_str()?.to_string();
                    let version = app["version"].as_str().unwrap_or("").to_string();
                    Some(SoftwareInfo { name, version })
                })
                .take(200) // 最大 200 件（データ量制限）
                .collect()
        }
        _ => vec![],
    }
}

/// Linux: dpkg --list でインストール済みパッケージを取得する（rpm フォールバックあり）
#[cfg(not(any(target_os = "windows", target_os = "macos")))]
fn collect_software_linux() -> Vec<SoftwareInfo> {
    // まず dpkg を試みる（Debian/Ubuntu 系）
    let dpkg_output = std::process::Command::new("dpkg")
        .args(["--list"])
        .output();

    if let Ok(out) = dpkg_output {
        if out.status.success() {
            let text = String::from_utf8_lossy(&out.stdout);
            return text
                .lines()
                .filter(|line| line.starts_with("ii ")) // インストール済みのみ
                .filter_map(|line| {
                    let parts: Vec<&str> = line.split_whitespace().collect();
                    if parts.len() >= 3 {
                        Some(SoftwareInfo {
                            name:    parts[1].to_string(),
                            version: parts[2].to_string(),
                        })
                    } else {
                        None
                    }
                })
                .take(200)
                .collect();
        }
    }

    // dpkg が使えない場合は rpm を試みる（RHEL/CentOS 系）
    let rpm_output = std::process::Command::new("rpm")
        .args(["-qa", "--queryformat", "%{NAME},%{VERSION}\n"])
        .output();

    match rpm_output {
        Ok(out) if out.status.success() => {
            let text = String::from_utf8_lossy(&out.stdout);
            text.lines()
                .filter_map(|line| {
                    let mut parts = line.splitn(2, ',');
                    let name    = parts.next()?.to_string();
                    let version = parts.next().unwrap_or("").to_string();
                    Some(SoftwareInfo { name, version })
                })
                .take(200)
                .collect()
        }
        _ => vec![],
    }
}

/// PowerShell の ConvertTo-Csv 出力（ヘッダー付き CSV）を SoftwareInfo の Vec に変換する
///
/// 1 行目: ヘッダー行（スキップ）
/// 2 行目以降: "Name","Version" 形式のデータ行
#[cfg(target_os = "windows")]
fn parse_csv_software(csv_text: &str) -> Vec<SoftwareInfo> {
    csv_text
        .lines()
        .skip(1) // ヘッダー行をスキップ
        .filter_map(|line| {
            // ダブルクォートと改行を除去してカンマで分割
            let clean = line.replace('"', "");
            let mut parts = clean.splitn(2, ',');
            let name    = parts.next()?.trim().to_string();
            let version = parts.next().unwrap_or("").trim().to_string();
            if name.is_empty() { return None; }
            Some(SoftwareInfo { name, version })
        })
        .take(200) // 最大 200 件（データ量・通信量の制限）
        .collect()
}

/// f64 値を小数点第1位で丸める（表示用）
fn round1(value: f64) -> f64 {
    (value * 10.0).round() / 10.0
}

// =====================================================
// アプリケーション起動
// =====================================================

/// Tauri アプリケーションを起動する。
/// Tauri Builder に Tauri コマンドを登録してアプリを実行する。
#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())
        // 公開する Tauri コマンドを登録する
        .invoke_handler(tauri::generate_handler![
            collect_pc_info, // PC 情報収集コマンド
            send_report,     // バックエンド送信コマンド
        ])
        .run(tauri::generate_context!())
        .expect("Tauriアプリケーションの起動に失敗しました");
}
