<!--
  機能ID: WEB_DSH0101
  views/WEB_DSH0101_DashboardView.vue
  -----------------------------------------------
  ダッシュボード画面コンポーネント
  -----------------------------------------------
-->
<template>
  <AppLayout>
    <div class="page">
      <!-- ページタイトル（imageフォルダのアイコン画像を使用。規約準拠） -->
      <h2 class="page-title">
        <img src="@/image/icon-dashboard.svg" class="page-title-icon" alt="ダッシュボード" />
        ダッシュボード
      </h2>

      <!-- KPI カード -->
      <div class="kpi-grid">
        <div class="kpi-card">
          <!-- KPIアイコン（imageフォルダより読み込み） -->
          <div class="kpi-icon"><img src="@/image/icon-pc.svg" alt="総PC台数" /></div>
          <div class="kpi-body">
            <div class="kpi-label">総PC台数</div>
            <div class="kpi-value">{{ statsLoading ? '…' : stats?.totalPcCount ?? '—' }}</div>
          </div>
        </div>
        <div class="kpi-card green">
          <div class="kpi-icon"><img src="@/image/icon-active.svg" alt="使用中" /></div>
          <div class="kpi-body">
            <div class="kpi-label">使用中</div>
            <div class="kpi-value">{{ statsLoading ? '…' : stats?.inUsePcCount ?? '—' }}</div>
          </div>
        </div>
        <div class="kpi-card blue">
          <div class="kpi-icon"><img src="@/image/icon-rental.svg" alt="保管中" /></div>
          <div class="kpi-body">
            <div class="kpi-label">保管中</div>
            <div class="kpi-value">{{ statsLoading ? '…' : stats?.inStoragePcCount ?? '—' }}</div>
          </div>
        </div>
        <div class="kpi-card orange">
          <div class="kpi-icon"><img src="@/image/icon-loan.svg" alt="貸出中PC" /></div>
          <div class="kpi-body">
            <div class="kpi-label">貸出中PC</div>
            <div class="kpi-value">{{ statsLoading ? '…' : stats?.activeLoansCount ?? '—' }}</div>
          </div>
        </div>
        <div class="kpi-card warning">
          <div class="kpi-icon"><img src="@/image/icon-warning.svg" alt="レンタル期限切れ間近" /></div>
          <div class="kpi-body">
            <div class="kpi-label">レンタル期限切れ間近</div>
            <div class="kpi-value">{{ statsLoading ? '…' : stats?.nearExpiryRentalsCount ?? '—' }}</div>
          </div>
        </div>
      </div>

      <!-- クイックリンク -->
      <div class="quick-links">
        <h3 class="section-title">クイックアクセス</h3>
        <div class="link-grid">
          <!-- クイックリンクアイコン（imageフォルダより読み込み） -->
          <div class="link-card" @click="router.push('/assets')">
            <img src="@/image/icon-pc.svg" class="link-icon" alt="PC一覧" />
            <div>
              <div class="link-title">PC一覧</div>
              <div class="link-desc">資産の確認・登録・編集</div>
            </div>
          </div>
          <div class="link-card" @click="router.push('/employees')">
            <img src="@/image/icon-employee.svg" class="link-icon" alt="社員管理" />
            <div>
              <div class="link-title">社員管理</div>
              <div class="link-desc">社員情報の管理</div>
            </div>
          </div>
          <div class="link-card" @click="router.push('/loans')">
            <img src="@/image/icon-loan.svg" class="link-icon" alt="貸出管理" />
            <div>
              <div class="link-title">貸出管理</div>
              <div class="link-desc">貸出・返却の登録と確認</div>
            </div>
          </div>
          <div class="link-card" @click="router.push('/rentals')">
            <img src="@/image/icon-rental.svg" class="link-icon" alt="レンタル管理" />
            <div>
              <div class="link-title">レンタル管理</div>
              <div class="link-desc">契約・期限アラート確認</div>
            </div>
          </div>
          <div class="link-card" @click="router.push('/software')">
            <img src="@/image/icon-software.svg" class="link-icon" alt="ソフトウェア" />
            <div>
              <div class="link-title">ソフトウェア</div>
              <div class="link-desc">ライセンス管理・超過確認</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { dashboardApi } from '@/api/dashboard'
import AppLayout from '@/components/AppLayout.vue'
import type { DashboardStats } from '@/types'

const router = useRouter()

const stats = ref<DashboardStats | null>(null)
const statsLoading = ref(false)

async function loadStats() {
  statsLoading.value = true
  try {
    const res = await dashboardApi.getStats()
    stats.value = res.data
  } catch { /* ネットワーク不可時は無視 */ }
  finally { statsLoading.value = false }
}

onMounted(loadStats)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 28px; }
.page-title { font-size: 20px; font-weight: 700; color: #1a1a2e; margin: 0; }

.kpi-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
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
.kpi-card.green   { border-left-color: #22c55e; }
.kpi-card.blue    { border-left-color: #3b82f6; }
.kpi-card.orange  { border-left-color: #f97316; }
.kpi-card.warning { border-left-color: #f59e0b; }
.kpi-card.danger  { border-left-color: #ef4444; }
.kpi-icon  { font-size: 30px; }
.kpi-label { font-size: 12px; color: #6b7280; margin-bottom: 4px; }
.kpi-value { font-size: 28px; font-weight: 700; color: #1a1a2e; }

.section-title { font-size: 15px; font-weight: 700; color: #1a1a2e; margin: 0 0 14px; }

.link-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 12px;
}
.link-card {
  background: white;
  border-radius: 10px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.07);
  cursor: pointer;
  transition: box-shadow 0.15s, transform 0.1s;
}
.link-card:not(.disabled):hover {
  box-shadow: 0 4px 12px rgba(0,0,0,0.12);
  transform: translateY(-1px);
}
.link-card.disabled { opacity: 0.5; cursor: not-allowed; }
.link-icon { font-size: 28px; }
.link-title { font-size: 14px; font-weight: 600; color: #1a1a2e; }
.link-desc  { font-size: 12px; color: #6b7280; margin-top: 2px; }
</style>
