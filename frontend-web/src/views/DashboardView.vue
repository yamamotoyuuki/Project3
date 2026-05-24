<template>
  <div class="dashboard">
    <!-- ヘッダー -->
    <header class="header">
      <div class="header-left">
        <span class="header-logo">🖥️</span>
        <h1 class="header-title">PC管理システム</h1>
      </div>
      <div class="header-right">
        <span class="user-info">{{ authStore.currentUser?.displayName }}</span>
        <span class="role-badge">{{ roleLabel }}</span>
        <button class="logout-btn" @click="handleLogout">ログアウト</button>
      </div>
    </header>

    <!-- サイドバー -->
    <div class="layout">
      <nav class="sidebar">
        <ul class="nav-list">
          <li class="nav-item active">
            <span class="nav-icon">📊</span> ダッシュボード
          </li>
          <li class="nav-item">
            <span class="nav-icon">💻</span> PC一覧
          </li>
          <li class="nav-item">
            <span class="nav-icon">📋</span> 貸出管理
          </li>
          <li class="nav-item">
            <span class="nav-icon">📦</span> レンタル管理
          </li>
          <li class="nav-item">
            <span class="nav-icon">🔑</span> ソフトウェア
          </li>
          <li class="nav-item">
            <span class="nav-icon">👥</span> 社員管理
          </li>
          <li v-if="authStore.isAdmin" class="nav-item">
            <span class="nav-icon">⚙️</span> ユーザー管理
          </li>
        </ul>
      </nav>

      <!-- メインコンテンツ -->
      <main class="main-content">
        <h2 class="page-title">ダッシュボード</h2>

        <!-- KPIカード（Phase 2 で実データに切り替え） -->
        <div class="kpi-grid">
          <div class="kpi-card">
            <div class="kpi-icon">💻</div>
            <div class="kpi-body">
              <div class="kpi-label">総PC台数</div>
              <div class="kpi-value">—</div>
            </div>
          </div>
          <div class="kpi-card warning">
            <div class="kpi-icon">⚠️</div>
            <div class="kpi-body">
              <div class="kpi-label">未返却PC（貸出中）</div>
              <div class="kpi-value">—</div>
            </div>
          </div>
          <div class="kpi-card danger">
            <div class="kpi-icon">🔔</div>
            <div class="kpi-body">
              <div class="kpi-label">レンタル期限切れ間近</div>
              <div class="kpi-value">—</div>
            </div>
          </div>
          <div class="kpi-card">
            <div class="kpi-icon">🔑</div>
            <div class="kpi-body">
              <div class="kpi-label">ライセンス超過ソフト</div>
              <div class="kpi-value">—</div>
            </div>
          </div>
        </div>

        <div class="placeholder-msg">
          <p>Phase 2 でPC資産管理機能を実装予定です。</p>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const router = useRouter()

const roleLabel = computed(() => {
  switch (authStore.currentUser?.role) {
    case 'ADMIN': return '管理者'
    case 'IT_STAFF': return 'IT担当'
    case 'VIEWER': return '閲覧者'
    default: return ''
  }
})

async function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.dashboard {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f3f4f6;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 56px;
  background: #1a1a2e;
  color: white;
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.header-logo { font-size: 22px; }
.header-title { font-size: 17px; font-weight: 700; }

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info { font-size: 14px; }
.role-badge {
  background: #6366f1;
  padding: 2px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.logout-btn {
  background: transparent;
  border: 1px solid rgba(255,255,255,0.4);
  color: white;
  padding: 4px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.2s;
}
.logout-btn:hover { background: rgba(255,255,255,0.1); }

.layout {
  display: flex;
  flex: 1;
}

.sidebar {
  width: 220px;
  background: white;
  border-right: 1px solid #e5e7eb;
  padding: 16px 0;
  flex-shrink: 0;
}

.nav-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  cursor: pointer;
  font-size: 14px;
  color: #374151;
  transition: background 0.15s;
}

.nav-item:hover { background: #f3f4f6; }
.nav-item.active {
  background: #ede9fe;
  color: #6366f1;
  font-weight: 600;
}

.nav-icon { font-size: 16px; }

.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}

.page-title {
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 24px;
}

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 32px;
}

.kpi-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 16px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.07);
  border-left: 4px solid #6366f1;
}

.kpi-card.warning { border-left-color: #f59e0b; }
.kpi-card.danger  { border-left-color: #ef4444; }

.kpi-icon { font-size: 32px; }
.kpi-label { font-size: 12px; color: #6b7280; margin-bottom: 4px; }
.kpi-value { font-size: 28px; font-weight: 700; color: #1a1a2e; }

.placeholder-msg {
  background: white;
  border-radius: 10px;
  padding: 40px;
  text-align: center;
  color: #9ca3af;
  font-size: 15px;
}
</style>
