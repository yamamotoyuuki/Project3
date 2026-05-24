<template>
  <div class="agent-app">
    <header class="agent-header">
      <span>🖥️ PC管理エージェント</span>
      <span class="version">v1.0.0</span>
    </header>

    <!-- PC情報表示タブ (画面A) -->
    <div v-if="currentView === 'info'" class="view-panel">
      <h2>PC情報</h2>
      <div v-if="pcInfo" class="info-grid">
        <div class="info-row"><span class="info-label">ホスト名</span><span>{{ pcInfo.hostname }}</span></div>
        <div class="info-row"><span class="info-label">CPU</span><span>{{ pcInfo.hardware.cpu_model }}</span></div>
        <div class="info-row"><span class="info-label">コア数</span><span>{{ pcInfo.hardware.cpu_cores }}</span></div>
        <div class="info-row"><span class="info-label">メモリ</span><span>{{ pcInfo.hardware.memory_gb.toFixed(1) }} GB</span></div>
        <div class="info-row"><span class="info-label">OS</span><span>{{ pcInfo.os.name }} {{ pcInfo.os.version }}</span></div>
        <div class="info-row"><span class="info-label">収集日時</span><span>{{ pcInfo.collected_at }}</span></div>
      </div>
      <button class="btn-primary" @click="collectInfo">情報を取得</button>
    </div>

    <!-- 送信タブ (画面B) -->
    <div v-if="currentView === 'send'" class="view-panel">
      <h2>情報送信</h2>
      <p class="status-msg" :class="sendStatus.type">{{ sendStatus.message }}</p>
      <button class="btn-primary" @click="sendReport" :disabled="!pcInfo">
        APIへ送信
      </button>
    </div>

    <!-- 設定タブ (画面C) -->
    <div v-if="currentView === 'settings'" class="view-panel">
      <h2>設定</h2>
      <div class="form-group">
        <label>管理番号（例: PC-00123）</label>
        <input v-model="settings.assetNumber" placeholder="PC-" />
      </div>
      <div class="form-group">
        <label>API URL</label>
        <input v-model="settings.apiUrl" placeholder="https://管理サーバー/api/v1" />
      </div>
      <div class="form-group">
        <label>設置場所</label>
        <input v-model="settings.location" placeholder="3F-営業部" />
      </div>
      <button class="btn-primary" @click="saveSettings">保存</button>
    </div>

    <!-- ボトムナビ -->
    <nav class="bottom-nav">
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
import { ref, onMounted } from 'vue'
import { invoke } from '@tauri-apps/api/core'

type View = 'info' | 'send' | 'settings'

interface PcInfo {
  hostname: string
  hardware: { cpu_model: string; cpu_cores: number; memory_gb: number; disk_gb: number; disk_free_gb: number }
  os: { name: string; version: string }
  network: { ip: string; mac: string }[]
  software: { name: string; version: string }[]
  collected_at: string
}

const currentView = ref<View>('info')
const pcInfo = ref<PcInfo | null>(null)
const sendStatus = ref({ type: '', message: '送信ボタンを押してください' })
const settings = ref({
  assetNumber: localStorage.getItem('assetNumber') || '',
  apiUrl: localStorage.getItem('apiUrl') || 'http://localhost:8080/api/v1',
  location: localStorage.getItem('location') || '',
})

async function collectInfo() {
  try {
    pcInfo.value = await invoke<PcInfo>('collect_pc_info')
  } catch (e) {
    console.error('情報収集エラー', e)
  }
}

async function sendReport() {
  if (!pcInfo.value) return
  try {
    sendStatus.value = { type: 'loading', message: '送信中...' }
    const report = {
      ...pcInfo.value,
      asset_number: settings.value.assetNumber,
      location: settings.value.location,
    }
    await invoke('send_report', { apiUrl: settings.value.apiUrl, report })
    sendStatus.value = { type: 'success', message: '✅ 送信成功しました' }
  } catch (e: any) {
    sendStatus.value = { type: 'error', message: `❌ ${e}` }
  }
}

function saveSettings() {
  localStorage.setItem('assetNumber', settings.value.assetNumber)
  localStorage.setItem('apiUrl', settings.value.apiUrl)
  localStorage.setItem('location', settings.value.location)
  alert('設定を保存しました')
}

onMounted(() => {
  collectInfo()
})
</script>

<style scoped>
.agent-app {
  display: flex;
  flex-direction: column;
  height: 100vh;
  font-family: 'Segoe UI', sans-serif;
  background: #f3f4f6;
}

.agent-header {
  background: #1a1a2e;
  color: white;
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
}

.version { font-size: 12px; opacity: 0.6; }

.view-panel {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
}

.view-panel h2 { font-size: 18px; margin-bottom: 16px; color: #1a1a2e; }

.info-grid { display: flex; flex-direction: column; gap: 10px; margin-bottom: 20px; }

.info-row {
  display: flex;
  justify-content: space-between;
  background: white;
  padding: 10px 14px;
  border-radius: 8px;
  font-size: 14px;
}

.info-label { color: #6b7280; font-weight: 500; }

.form-group { margin-bottom: 16px; }
.form-group label { display: block; font-size: 13px; color: #374151; margin-bottom: 4px; }
.form-group input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 14px;
}

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

.btn-primary:disabled { opacity: 0.5; cursor: not-allowed; }

.status-msg { padding: 10px; border-radius: 8px; margin-bottom: 16px; font-size: 14px; }
.status-msg.success { background: #d1fae5; color: #065f46; }
.status-msg.error { background: #fee2e2; color: #dc2626; }
.status-msg.loading { background: #ede9fe; color: #5b21b6; }

.bottom-nav {
  display: flex;
  border-top: 1px solid #e5e7eb;
  background: white;
}

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

.bottom-nav button.active {
  color: #6366f1;
  font-weight: 600;
  border-top: 2px solid #6366f1;
}
</style>
