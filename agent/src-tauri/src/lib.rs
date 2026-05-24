use serde::{Deserialize, Serialize};
use sysinfo::System;

// =====================
// データ構造（エージェントが収集する情報）
// =====================

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct HardwareInfo {
    pub cpu_model: String,
    pub cpu_cores: usize,
    pub memory_gb: f64,
    pub disk_gb: f64,
    pub disk_free_gb: f64,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct OsInfo {
    pub name: String,
    pub version: String,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct NetworkInfo {
    pub ip: String,
    pub mac: String,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct SoftwareInfo {
    pub name: String,
    pub version: String,
}

#[derive(Debug, Serialize, Deserialize, Clone)]
pub struct AgentReport {
    pub asset_number: String,
    pub location: String,
    pub hostname: String,
    pub hardware: HardwareInfo,
    pub os: OsInfo,
    pub network: Vec<NetworkInfo>,
    pub software: Vec<SoftwareInfo>,
    pub collected_at: String,
}

// =====================
// Tauri コマンド: PC情報を収集する
// =====================

#[tauri::command]
fn collect_pc_info() -> Result<AgentReport, String> {
    let mut sys = System::new_all();
    sys.refresh_all();

    // ホスト名を取得
    let hostname = hostname::get()
        .map(|h| h.to_string_lossy().to_string())
        .unwrap_or_else(|_| "unknown".to_string());

    // CPU 情報
    let cpu_model = sys
        .cpus()
        .first()
        .map(|c| c.brand().to_string())
        .unwrap_or_else(|| "Unknown CPU".to_string());
    let cpu_cores = sys.cpus().len();

    // メモリ情報（バイト → GB）
    let memory_gb = sys.total_memory() as f64 / 1024.0 / 1024.0 / 1024.0;

    // ディスク情報（Phase 2 で sysinfo::Disks を使って実装）
    let disk_gb = 0.0_f64;
    let disk_free_gb = 0.0_f64;

    // OS情報
    let os_name = System::name().unwrap_or_else(|| "Unknown".to_string());
    let os_version = System::os_version().unwrap_or_else(|| "Unknown".to_string());

    // 現在日時（ISO 8601）
    let collected_at = chrono::Local::now().to_rfc3339();

    let report = AgentReport {
        asset_number: String::new(), // ユーザーが設定画面で入力
        location: String::new(),
        hostname,
        hardware: HardwareInfo {
            cpu_model,
            cpu_cores,
            memory_gb: (memory_gb * 10.0).round() / 10.0,
            disk_gb,
            disk_free_gb,
        },
        os: OsInfo {
            name: os_name,
            version: os_version,
        },
        network: vec![], // Phase 2 で実装
        software: vec![], // Phase 2 で実装
        collected_at,
    };

    Ok(report)
}

// =====================
// Tauri コマンド: APIへ送信する
// =====================

#[tauri::command]
async fn send_report(api_url: String, report: AgentReport) -> Result<String, String> {
    let client = reqwest::Client::new();
    let url = format!("{}/agent/report", api_url.trim_end_matches('/'));

    let response = client
        .post(&url)
        .json(&report)
        .send()
        .await
        .map_err(|e| format!("送信エラー: {}", e))?;

    if response.status().is_success() {
        Ok("送信成功".to_string())
    } else {
        Err(format!("サーバーエラー: {}", response.status()))
    }
}

// =====================
// アプリケーション起動
// =====================

#[cfg_attr(mobile, tauri::mobile_entry_point)]
pub fn run() {
    tauri::Builder::default()
        .plugin(tauri_plugin_shell::init())
        .invoke_handler(tauri::generate_handler![
            collect_pc_info,
            send_report,
        ])
        .run(tauri::generate_context!())
        .expect("Tauriアプリケーションの起動に失敗しました");
}
