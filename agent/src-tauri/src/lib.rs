/**
 * lib.rs
 * -----------------------------------------------
 * PC管理エージェント - コアロジック
 *
 * Tauri コマンドとして以下の機能を提供する:
 *   - load_config    : application.yml からAPI設定を読み込む
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
// 設定ファイル構造体（application.yml のマッピング）
// =====================================================

/// application.yml のルート構造
///
/// ```yaml
/// agent:
///   api:
///     base-url: http://localhost:8080/api/v1
/// ```
#[derive(Debug, Deserialize)]
struct AppConfig {
    /// エージェント設定セクション
    agent: AgentSection,
}

/// `agent:` セクションの構造
#[derive(Debug, Deserialize)]
struct AgentSection {
    /// API接続情報セクション
    api: ApiSection,
}

/// `api:` セクションの構造
#[derive(Debug, Deserialize, Serialize, Clone)]
pub struct ApiSection {
    /// バックエンドAPIのベースURL（例: "http://localhost:8080/api/v1"）
    #[serde(rename = "base-url")]
    pub base_url: String,
}

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
    /// エージェント番号（初回登録時にバックエンドが発行。ローカルに保存して毎回送信する）
    /// Option<String> とすることで、未取得（null）を JSON に含めずシリアライズできる
    pub agent_number: Option<String>,
    /// PC の資産番号（ユーザーが設定画面で入力）
    pub asset_number: String,
    /// 設置場所（ユーザーが設定画面で入力。pc_assets.location に登録される）
    pub location: String,
    /// 使用者名（ユーザーが設定画面で入力。pc_assets.user_name に登録される）
    /// Option<String> とすることで、未入力（null）を JSON に含めずシリアライズできる
    pub user_name: Option<String>,
    /// 取得区分（"PURCHASE" / "RENTAL"）
    /// - エージェントから初めて設定する場合のみ送信する（バックエンドに既に設定済みの場合は None）
    /// - Option<String> とすることで、送信不要（null）を JSON に含めずシリアライズできる
    pub acquisition_type: Option<String>,
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
// Tauri コマンド: 設定ファイルを読み込む
// =====================================================

/// application.yml からエージェント設定（APIのベースURL等）を読み込む。
///
/// 読み込み優先順位:
///   1. アプリのリソースディレクトリ内の application.yml（バンドル済み）
///   2. 実行ファイルと同じディレクトリの application.yml（管理者が配置）
///
/// いずれも読み込めない場合はデフォルト値（http://localhost:8080/api/v1）を返す。
///
/// # Returns
/// 読み込み成功時は `Ok(ApiSection)`、失敗時は `Err(エラーメッセージ)` を返す。
/// フロントエンド側では `result.base_url` でAPIのベースURLを取得する。
#[tauri::command]
fn load_config(app: tauri::AppHandle) -> Result<ApiSection, String> {
    use tauri::Manager;

    // ---- ① リソースディレクトリから読み込む（Tauri バンドル済み）----
    let resource_path = app
        .path()
        .resource_dir()
        .ok()
        .map(|dir| dir.join("application.yml"));

    if let Some(path) = resource_path {
        if path.exists() {
            match read_config_from_path(&path) {
                Ok(section) => {
                    println!("[CONFIG] application.yml をリソースディレクトリから読み込みました: {:?}", path);
                    return Ok(section);
                }
                Err(e) => {
                    eprintln!("[CONFIG] リソースディレクトリの application.yml 読み込みエラー: {}", e);
                }
            }
        }
    }

    // ---- ② 実行ファイルと同階層から読み込む（管理者配置用）----
    let exe_path = std::env::current_exe()
        .ok()
        .and_then(|p| p.parent().map(|d| d.join("application.yml")));

    if let Some(path) = exe_path {
        if path.exists() {
            match read_config_from_path(&path) {
                Ok(section) => {
                    println!("[CONFIG] application.yml を実行ファイルディレクトリから読み込みました: {:?}", path);
                    return Ok(section);
                }
                Err(e) => {
                    eprintln!("[CONFIG] 実行ファイルディレクトリの application.yml 読み込みエラー: {}", e);
                }
            }
        }
    }

    // ---- ③ いずれも見つからない場合はデフォルト値を返す ----
    println!("[CONFIG] application.yml が見つかりませんでした。デフォルト値を使用します。");
    Ok(ApiSection {
        base_url: "http://localhost:8080/api/v1".to_string(),
    })
}

/// 指定パスの YAML ファイルを読み込んで ApiSection を返すヘルパー関数
///
/// # Arguments
/// * `path` - 読み込む YAML ファイルのパス
///
/// # Returns
/// 読み込み・パース成功時は `Ok(ApiSection)`、失敗時は `Err(エラーメッセージ)` を返す。
fn read_config_from_path(path: &std::path::Path) -> Result<ApiSection, String> {
    let content = std::fs::read_to_string(path)
        .map_err(|e| format!("ファイル読み込みエラー: {}", e))?;

    let config: AppConfig = serde_yaml::from_str(&content)
        .map_err(|e| format!("YAML 解析エラー: {}", e))?;

    Ok(config.agent.api)
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
        agent_number:    None,           // ローカルに保存したエージェント番号（Vue側で上書き）
        asset_number:    String::new(),  // 設定画面でユーザーが入力する値（Vue側で上書き）
        location:        String::new(),  // 設定画面でユーザーが入力する値（Vue側で上書き）
        user_name:       None,           // 設定画面でユーザーが入力する値（Vue側で上書き）
        acquisition_type: None,          // 保存ボタン押下時にVue側で設定（バックエンド未設定時のみ）
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
// Tauri コマンド: 取得区分の取得
// =====================================================

/// エージェント起動時にバックエンドから PC 資産の取得区分（購入/レンタル）を取得する。
///
/// バックエンドに取得区分が設定済みの場合は "PURCHASE" または "RENTAL" を返す。
/// 対応する PC 資産が見つからない、または未設定の場合は `None` を返す。
/// エラー時（バックエンド未起動等）も `None` を返し、設定欄を活性化する。
///
/// 検索順序（バックエンド側）:
///   1. エージェント番号で検索（2回目以降・pc_assets.agent_number が設定済みの場合）
///   2. ホスト名でフォールバック検索（管理者が先に資産登録した場合など、agent_number 未紐付け時）
///
/// # Arguments
/// * `api_url`      - バックエンドの API ベース URL（例: "http://localhost:8080/api/v1"）
/// * `agent_number` - エージェント番号（例: "AGT-A1B2C3D4"）
/// * `hostname`     - エージェントが動作する PC のホスト名（フォールバック検索に使用）
///
/// # Returns
/// バックエンドに設定済みの場合は `Ok(Some("PURCHASE"))` / `Ok(Some("RENTAL"))`、
/// 未設定・未登録・エラーの場合は `Ok(None)` を返す。
#[tauri::command]
async fn fetch_asset_acquisition_type(
    api_url: String,
    agent_number: String,
    hostname: String,
) -> Result<Option<String>, String> {
    let client = reqwest::Client::new();
    // URL を組み立てる（agentNumber + hostname の両方をクエリパラメータで送信）
    let url = format!(
        "{}/agent/asset-info?agentNumber={}&hostname={}",
        api_url.trim_end_matches('/'),
        agent_number,
        hostname
    );

    let response = client
        .get(&url)
        .send()
        .await
        .map_err(|e| format!("取得エラー: {}", e))?;

    if response.status().is_success() {
        // レスポンス例: {"success": true, "data": "RENTAL"} または {"success": true, "data": null}
        let json: serde_json::Value = response
            .json()
            .await
            .map_err(|e| format!("レスポンス解析エラー: {}", e))?;

        // "data" フィールドが文字列の場合のみ返す（null の場合は None）
        Ok(json["data"].as_str().map(|s| s.to_string()))
    } else {
        // 取得失敗時（資産未登録等）は None を返して設定欄を活性化する
        Ok(None)
    }
}

// =====================================================
// Tauri コマンド: エージェント番号のファイル保存・読み込み
// =====================================================

/// エージェント番号を保存するファイル名
/// - Unix 系 OS: `.` 始まりにより隠しファイル扱い
/// - Windows: attrib +H で明示的に隠し属性を付与する
const AGENT_NUMBER_FILE: &str = ".agent_id";

/// エージェント番号をアプリのローカルデータディレクトリから読み込む。
///
/// 保存先:
/// - Windows: `%LOCALAPPDATA%\{app-name}\.agent_id`
/// - macOS:   `~/Library/Application Support/{app-name}/.agent_id`
/// - Linux:   `~/.local/share/{app-name}/.agent_id`
///
/// # Returns
/// ファイルが存在する場合は `Ok(Some("AGT-XXXXXXXX"))`、
/// ファイルがない場合は `Ok(None)`、
/// 読み込みエラーの場合は `Err(エラーメッセージ)` を返す。
#[tauri::command]
fn load_agent_number(app: tauri::AppHandle) -> Result<Option<String>, String> {
    use tauri::Manager;

    // Tauri が管理するアプリローカルデータディレクトリを取得する
    let data_dir = app
        .path()
        .app_local_data_dir()
        .map_err(|e| format!("データディレクトリ取得エラー: {}", e))?;

    let file_path = data_dir.join(AGENT_NUMBER_FILE);

    // ファイルが存在しない場合（初回起動など）は None を返す
    if !file_path.exists() {
        return Ok(None);
    }

    // ファイルを読み込んでトリミングする
    let content = std::fs::read_to_string(&file_path)
        .map_err(|e| format!("エージェント番号読み込みエラー: {}", e))?;

    let number = content.trim().to_string();
    if number.is_empty() {
        Ok(None)
    } else {
        Ok(Some(number))
    }
}

/// エージェント番号をアプリのローカルデータディレクトリに保存する。
///
/// Unix 系 OS では `.` 始まりのファイル名が隠しファイルとして扱われる。
/// Windows では `attrib +H` コマンドで隠し属性を付与する。
///
/// # Arguments
/// * `app`          - Tauri アプリハンドル（保存先ディレクトリの解決に使用）
/// * `agent_number` - 保存するエージェント番号（例: "AGT-A1B2C3D4"）
///
/// # Returns
/// 保存成功時は `Ok(())`、失敗時は `Err(エラーメッセージ)` を返す。
#[tauri::command]
fn save_agent_number(app: tauri::AppHandle, agent_number: String) -> Result<(), String> {
    use tauri::Manager;

    // Tauri が管理するアプリローカルデータディレクトリを取得する
    let data_dir = app
        .path()
        .app_local_data_dir()
        .map_err(|e| format!("データディレクトリ取得エラー: {}", e))?;

    // ディレクトリが存在しない場合は再帰的に作成する
    std::fs::create_dir_all(&data_dir)
        .map_err(|e| format!("ディレクトリ作成エラー: {}", e))?;

    let file_path = data_dir.join(AGENT_NUMBER_FILE);

    // エージェント番号をファイルに書き込む
    std::fs::write(&file_path, &agent_number)
        .map_err(|e| format!("エージェント番号書き込みエラー: {}", e))?;

    // Windows の場合は attrib コマンドでファイルに隠し属性（+H）を付与する
    // （Unix 系では `.` 始まりのファイル名が自動的に隠しファイル扱いになるため不要）
    #[cfg(target_os = "windows")]
    {
        if let Some(path_str) = file_path.to_str() {
            // attrib +H でファイルを隠しファイルに設定する
            // 失敗してもファイル自体の保存は成功しているため、エラーは無視する
            let _ = std::process::Command::new("attrib")
                .args(["+H", path_str])
                .output();
        }
    }

    Ok(())
}

// =====================================================
// Tauri コマンド: エージェントを初回登録する
// =====================================================

/// エージェントを初回登録してエージェント番号を発行してもらう。
///
/// エージェントアプリ起動時にローカルファイルにエージェント番号が存在しない場合に呼び出す。
/// バックエンドの POST /api/v1/agent/register にホスト名を送信し、
/// 発行されたエージェント番号（"AGT-XXXXXXXX" 形式）を返す。
///
/// # Arguments
/// * `api_url`  - バックエンドの API ベース URL（例: "http://localhost:8080/api/v1"）
/// * `hostname` - エージェントが動作する PC のホスト名
///
/// # Returns
/// 登録成功時は `Ok("AGT-XXXXXXXX")` 形式のエージェント番号、
/// 失敗時は `Err(エラーメッセージ)` を返す。
#[tauri::command]
async fn register_agent(api_url: String, hostname: String) -> Result<String, String> {
    let client = reqwest::Client::new();
    // URL を組み立てる（末尾スラッシュを除去してから追記）
    let url = format!("{}/agent/register", api_url.trim_end_matches('/'));

    // リクエストボディ: {"hostname": "..."}
    let body = serde_json::json!({ "hostname": hostname });

    let response = client
        .post(&url)
        .json(&body)
        .send()
        .await
        .map_err(|e| format!("登録エラー: {}", e))?;

    if response.status().is_success() {
        // レスポンス例: {"success": true, "data": "AGT-A1B2C3D4"}
        let json: serde_json::Value = response
            .json()
            .await
            .map_err(|e| format!("レスポンス解析エラー: {}", e))?;

        // "data" フィールドからエージェント番号を取得する
        let agent_number = json["data"]
            .as_str()
            .ok_or_else(|| "エージェント番号が取得できませんでした".to_string())?
            .to_string();

        Ok(agent_number)
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
        // ---- デバッグ設定 ----
        // debug_assertions が true のビルド（`npm run tauri dev` = デバッグビルド）時のみ
        // DevTools（ブラウザインスペクタ）を自動で開く。
        // リリースビルド（`npm run tauri build`）では実行されない。
        .setup(|app| {
            #[cfg(debug_assertions)]
            {
                use tauri::Manager;
                // メインウィンドウの DevTools を自動起動する
                // Vue の console.log / console.error や Vue DevTools が利用可能になる
                if let Some(window) = app.get_webview_window("main") {
                    window.open_devtools();
                    println!("[DEBUG] DevTools を起動しました");
                }
            }
            Ok(())
        })
        // 公開する Tauri コマンドを登録する
        .invoke_handler(tauri::generate_handler![
            load_config,                 // application.yml から API 設定を読み込むコマンド
            collect_pc_info,             // PC 情報収集コマンド
            send_report,                 // バックエンド送信コマンド
            register_agent,              // エージェント初回登録コマンド
            load_agent_number,           // エージェント番号をファイルから読み込むコマンド
            save_agent_number,           // エージェント番号をファイルに保存するコマンド
            fetch_asset_acquisition_type, // 起動時に取得区分をバックエンドから取得するコマンド
        ])
        .run(tauri::generate_context!())
        .expect("Tauriアプリケーションの起動に失敗しました");
}
