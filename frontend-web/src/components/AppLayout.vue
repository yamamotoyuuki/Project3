<template>
  <div class="app-shell">
    <!-- ヘッダー -->
    <header class="header">
      <div class="header-left">
        <!-- imageフォルダのアイコン画像を読み込む（規約: アイコンは画像化してimageフォルダに格納） -->
        <img src="@/image/icon-pc.svg" class="header-logo" alt="PC管理システム" />
        <h1 class="header-title">PC管理システム</h1>
      </div>
      <div class="header-right">
        <span class="user-info">{{ authStore.currentUser?.displayName }}</span>
        <span class="role-badge">{{ roleLabel }}</span>
        <button class="logout-btn" @click="handleLogout">ログアウト</button>
      </div>
    </header>

    <div class="body">
      <!-- サイドバー -->
      <nav class="sidebar">
        <ul class="nav-list">
          <li
            v-for="item in navItems"
            :key="item.to"
            class="nav-item"
            :class="{ active: isActive(item.to) }"
            @click="router.push(item.to)"
          >
            <!-- imageフォルダの画像ファイルをアイコンとして読み込む（規約準拠） -->
            <img :src="item.icon" class="nav-icon" :alt="item.label" />
            {{ item.label }}
          </li>
        </ul>
      </nav>

      <!-- メインコンテンツ -->
      <main class="main-content">
        <slot />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// ナビゲーションアイコン画像（imageフォルダより読み込み。規約: アイコンは画像化してimageフォルダに格納）
import iconDashboard from '@/image/icon-dashboard.svg'
import iconPc        from '@/image/icon-pc.svg'
import iconEmployee  from '@/image/icon-employee.svg'
import iconLoan      from '@/image/icon-loan.svg'
import iconRental    from '@/image/icon-rental.svg'
import iconSoftware  from '@/image/icon-software.svg'
import iconSettings  from '@/image/icon-settings.svg'

const authStore = useAuthStore()
const router = useRouter()
const route = useRoute()

const roleLabel = computed(() => {
  switch (authStore.currentUser?.role) {
    case 'ADMIN':    return '管理者'
    case 'IT_STAFF': return 'IT担当'
    case 'VIEWER':   return '閲覧者'
    default:         return ''
  }
})

// アイコンは絵文字でなくimageフォルダの画像ファイルパスを格納する（規約準拠）
const navItems = computed(() => {
  const items = [
    { to: '/dashboard',  icon: iconDashboard, label: 'ダッシュボード' },
    { to: '/assets',     icon: iconPc,        label: '機器一覧' },
    { to: '/employees',  icon: iconEmployee,  label: '社員管理' },
    { to: '/loans',      icon: iconLoan,      label: '貸出管理' },
    { to: '/rentals',    icon: iconRental,    label: 'レンタル管理' },
    { to: '/software',   icon: iconSoftware,  label: 'ソフトウェア' },
  ]
  if (authStore.isAdmin) {
    items.push({ to: '/users', icon: iconSettings, label: 'ユーザー管理' })
  }
  return items
})

function isActive(to: string): boolean {
  return route.path === to || route.path.startsWith(to + '/')
}

async function handleLogout() {
  authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f3f4f6;
}

/* ---- Header ---- */
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  height: 56px;
  background: #1a1a2e;
  color: white;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-left { display: flex; align-items: center; gap: 10px; }
.header-logo  { width: 26px; height: 26px; filter: brightness(0) invert(1); }
.header-title { font-size: 17px; font-weight: 700; }
.header-right { display: flex; align-items: center; gap: 12px; }
.user-info    { font-size: 14px; }
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

/* ---- Body ---- */
.body { display: flex; flex: 1; overflow: hidden; }

/* ---- Sidebar ---- */
.sidebar {
  width: 220px;
  background: white;
  border-right: 1px solid #e5e7eb;
  padding: 16px 0;
  flex-shrink: 0;
  overflow-y: auto;
}
.nav-list { list-style: none; padding: 0; margin: 0; }
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
.nav-icon { width: 18px; height: 18px; flex-shrink: 0; }

/* ---- Main ---- */
.main-content {
  flex: 1;
  padding: 24px;
  overflow-y: auto;
}
</style>
