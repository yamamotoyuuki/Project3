<!--
  agent/src/App.vue
  -----------------------------------------------
  PC管理エージェント - メインコンポーネント（Tauri デスクトップアプリ）

  3画面（タブ）構成:
    - 📻 PC情報: 収集した PC のハードウェア・OS 情報を表示
    - 📤 送信: 収集情報をバックエンド API へ POST 送信
    - ⚙️ 設定: 資産番号・API URL・設置場所を設定して永続化

  Tauri コマンド（Rust 側 lib.rs で定義）:
    - collect_pc_info(): ハードウェア情報の収集
    - send_report(apiUrl, report): バックエンドへのデータ送信
  -----------------------------------------------
-->
<template>
  <!-- アプリ全体ラッパー（縦方向レイアウト） -->
  <div class="agent-app">

    <!-- ヘッダー（アプリ名・バージョン表示） -->
    <header class="agent-header">
      <span>🖥️ PC管理エージェント</span>
      <span class="version">v1.0.0</span>
    </header>

    <!-- =====================
         タブ: PC情報表示（画面A）
         ===================== -->
    <div v-if="currentView === 'info'" class="view-panel">
      <h2>PC情報</h2>
      <!-- 収集済みの場合のみ情報グリッドを表示 -->
      <div v-if="pcInfo" class="info-grid">
        <div class="info-row">
          <span class="info-label">ホスト名</span>
          <span>{{ pcInfo.hostname }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">CPU</span>
          <span>{{ pcInfo.hardware.cpu_model }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">コア数</span>
          <span>{{ pcInfo.hardware.cpu_cores }} コア</span>
        </div>
        <div class="info-row">
          <span class="info-label">メモリ</span>
          <span>{{ pcInfo.hardware.memory_gb.toFixed(1) }} GB</span>
        </div>
        <div class="info-row">
          <span class="info-label">ディスク（総容量）</span>
          <span>{{ pcInfo.hardware.disk_gb.toFixed(1) }} GB</span>
        </div>
        <div class="info-row">
          <span class="info-label">ディスク（空き）</span>
          <span>{{ pcInfo.hardware.disk_free_gb.toFixed(1) }} GB</span>
        </div>
        <div class="info-row">
          <span class="info-label">OS</span>
          <span>{{ pcInfo.os.name }} {{ pcInfo.os.version }}</span>
        </div>
        <!-- ネットワーク IP アドレス（複数 NIC 対応） -->
        <div v-for="(nic, idx) in pcInfo.network" :key="idx" class="info-row">
          <span class="info-label">IPアドレス {{ idx > 0 ? idx + 1 : '' }}</span>
          <span>{{ nic.ip }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">収集日時</span>
          <span>{{ pcInfo.collected_at }}</span>
        </div>
        <!-- インストール済みソフトウェア件数 -->
        <div class="info-row">
          <span class="info-label">検出ソフトウェア</span>
          <span>{{ pcInfo.software.length }} 件</span>
        </div>
      </div>

      <!-- 情報収集ボタン（Tauri コマンドを呼び出す） -->
      <button class="btn-primary" @click="collectInfo">情報を取得</button>
    </div>

    <!-- =====================
         タブ: 送信（画面B）
         ===================== -->
    <div v-if="currentView === 'send'" class="view-panel">
      <h2>情報送信</h2>

      <!-- 送信前に資産番号の設定を促す警告 -->
      <div v-if="!settings.assetNumber" class="warning-alert">
        ⚠️ 設定画面で管理番号（資産番号）を入力してください
      </div>

      <!-- 送信ステータスメッセージ -->
      <p class="status-msg" :class="sendStatus.type">{{ sendStatus.message }}</p>

      <!-- 送信ボタン（PC情報未収集の場合は disabled） -->
      <button class="btn-primary" @click="sendReport" :disabled="!pcInfo || !settings.assetNumber">
        APIへ送信
      </button>
    </div>

    <!-- =====================
         タブ: 設定（画面C）
         ===================== -->
    <div v-if="currentView === 'settings'" class="view-panel">
      <h2>設定</h2>

      <!-- 管理番号入力 -->
      <div class="form-group">
        <label>管理番号（例: PC-00123）</label>
        <input v-model="settings.assetNumber" placeholder="PC-" />
      </div>

      <!-- バックエンド API URL 入力 -->
      <div class="form-group">
        <label>API URL</label>
        <input v-model="settings.apiUrl" placeholder="https://管理サーバー/api/v1" />
      </div>

      <!-- 設置場所入力 -->
      <div class="form-group">
        <label>設置場所</label>
        <input v-model="settings.location" placeholder="3F-営業部" />
      </div>

      <!-- 設定保存ボタン（localStorage に永続化） -->
      <button class="btn-primary" @click="saveSettings">保存</button>
    </div>

    <!-- =====================
         ボトムナビゲーション
         ===================== -->
    <nav class="bottom-nav">
      <!-- アクティブなタブは active クラスでハイライト -->
      <button :class="{ active: currentView === 'info' }" @click="currentView = 'info'">
        💻 PC情報
      </button>
      <button :class="{ active: currentView === 'send' }" @click="currentView = 'send'">
        📤 送信
      </button>
      <button :class="{ active: currentView === 'settings' }" @click="currentView = 'settings'">
        ⚙️ 設定
      </button>
    </nav>

  </div>
</template>

<script setup lang="ts">
/**
 * エージェントアプリのロジック
 *
 * - Tauri の invoke() で Rust コマンドを呼び出す
 * - 設定値は localStorage で永続化する
 * - 起動時に自動でPC情報を収集する
 */
import { ref, onMounted } from 'vue'
import { invoke } from '@tauri-apps/api/core'

/** タブ識別型 */
type View = 'info' | 'send' | 'settings'

/**
 * Tauri コマンド collect_pc_info() の戻り値型（Rust AgentReport に対応）
 */
interface PcInfo {
  hostname: string
  hardware: {
    cpu_model:    string
    cpu_cores:    number
    memory_gb:    number
    disk_gb:      number
    disk_free_gb: number
  }
  os: { name: string; version: string }
  network:  { ip: string; mac: string }[]
  software: { name: string; version: string }[]
  collected_at: string
}

/** 現在表示中のタブ */
const currentView = ref<View>('info')

/** 収集済み PC 情報（null = 未収集） */
const pcInfo = ref<PcInfo | null>(null)

/** 送信ステータスメッセージ（type: 'success' | 'error' | 'loading' | ''） */
const sendStatus = ref({ type: '', message: '送信ボタンを押してください' })

/**
 * 設定値（localStorage に永続化）
 * assetNumber: 資産番号（例: "PC-00123"）
 * apiUrl:      バックエンド API ベース URL
 * location:    設置場所
 */
const settings = ref({
  assetNumber: localStorage.getItem('assetNumber') || '',
  apiUrl:      localStorage.getItem('apiUrl')      || 'http://localhost:8080/api/v1',
  location:    localStorage.getItem('location')    || '',
})

/**
 * PC 情報を収集する
 * Rust の collect_pc_info コマンドを invoke() で呼び出す。
 * 成功時は pcInfo に結果をセット、失敗時はコンソールにエラーを出力する。
 */
async function collectInfo() {
  try {
    pcInfo.value = await invoke<PcInfo>('collect_pc_info')
  } catch (e) {
    console.error('情報収集エラー:', e)
  }
}

/**
 * 収集した PC 情報をバックエンドへ送信する
 * - 設定画面の assetNumber と location をレポートにマージしてから送信する
 * - Rust の send_report コマンドを invoke() で呼び出す
 */
async function sendReport() {
  if (!pcInfo.value) return
  try {
    sendStatus.value = { type: 'loading', message: '送信中...' }

    // 収集情報に資産番号と設置場所（ユーザー入力値）を追加してレポートを構築
    const report = {
      ...pcInfo.value,
      asset_number: settings.value.assetNumber,
      location:     settings.value.location,
    }

    // Rust コマンドでバックエンド API へ POST 送信
    await invoke('send_report', { apiUrl: settings.value.apiUrl, report })
    sendStatus.value = { type: 'success', message: '✅ 送信成功しました' }
  } catch (e: any) {
    sendStatus.value = { type: 'error', message: `❌ ${e}` }
  }
}

/**
 * 設定値を localStorage に保存する
 * アプリ再起動後も設定が引き継がれるよう永続化する。
 */
function saveSettings() {
  localStorage.setItem('assetNumber', settings.value.assetNumber)
  localStorage.setItem('apiUrl',      settings.value.apiUrl)
  localStorage.setItem('location',    settings.value.location)
  alert('設定を保存しました')
}

// アプリ起動時に自動でPC情報を収集する
onMounted(() => {
  collectInfo()
})
</script>

<style scoped>
/* ==============================
   アプリ全体レイアウト
   ============================== */

/* 縦方向に並べる全画面レイアウト */
.agent-app {
  display: flex;
  flex-direction: column;
  height: 100vh;
  font-family: 'Segoe UI', sans-serif;
  background: #f3f4f6;
}

/* ヘッダー（濃紺背景・白テキスト） */
.agent-header {
  background: #1a1a2e;
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

/* バージョン表示（小・半透明） */
.version { font-size: 12px; opacity: 0.6; }

/* ==============================
   タブコンテンツ
   ============================== */

/* 各タブの共通パネル */
.view-panel {
  flex: 1;           /* 残りのスペースを全て使用 */
  padding: 20px;
  overflow-y: auto;  /* コンテンツが多い場合はスクロール */
}

.view-panel h2 { font-size: 18px; margin-bottom: 16px; color: #1a1a2e; }

/* ==============================
   PC 情報グリッド
   ============================== */

/* 情報行を縦に並べるグリッド */
.info-grid { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }

/* 1行: ラベル左・値右 */
.info-row {
  display: flex;
  justify-content: space-between;
  background: white;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
}

/* 項目ラベル（グレー） */
.info-label { color: #6b7280; font-weight: 500; }

/* ==============================
   フォームグループ（設定画面）
   ============================== */

.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #374151; margin-bottom: 4px; }
.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
}

/* ==============================
   ボタン
   ============================== */

/* メインアクションボタン */
.btn-primary {
  background: #6366f1;
  color: white;
  border: none;
  padding: 12px 24px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  width: 100%;
}

/* disabled 状態: 半透明でカーソル変更 */
.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

/* ==============================
   ステータスメッセージ
   ============================== */

.status-msg { padding: 10px; border-radius: 8px; margin-bottom: 16px; font-size: 14px; }
.status-msg.success { background: #d1fae5; color: #065f46; } /* 緑: 送信成功 */
.status-msg.error   { background: #fee2e2; color: #dc2626; } /* 赤: エラー */
.status-msg.loading { background: #ede9fe; color: #5b21b6; } /* 紫: 送信中 */

/* 警告メッセージ（資産番号未設定時） */
.warning-alert {
  background: #fef9c3;
  border: 1px solid #fde047;
  color: #713f12;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
  margin-bottom: 16px;
}

/* ==============================
   ボトムナビゲーション
   ============================== */

/* 下部固定のタブバー */
.bottom-nav {
  display: flex;
  border-top: 1px solid #e5e7eb;
  background: white;
}

/* タブボタン共通 */
.bottom-nav button {
  flex: 1;
  padding: 14px 8px;
  border: none;
  background: transparent;
  cursor: pointer;
  font-size: 13px;
  color: #6b7280;
  transition: color 0.15s;
}

/* アクティブなタブ: インジゴ色・上部ボーダー */
.bottom-nav button.active {
  color: #6366f1;
  font-weight: 600;
  border-top: 2px solid #6366f1;
}
</style>
