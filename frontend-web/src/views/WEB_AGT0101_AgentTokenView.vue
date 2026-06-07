<!--
  機能ID: WEB_AGT0101
  views/WEB_AGT0101_AgentTokenView.vue
  -----------------------------------------------
  エージェント登録トークン管理画面

  機能概要:
    - エージェントアプリの初回登録に使用するトークンを発行・管理する
    - トークンは発行から24時間有効で、1回限り使用できる
    - 発行したトークンをエージェントの application.yml にコピーして設定する

  アクセス権限: ADMIN / OPERATOR
  -----------------------------------------------
-->
<template>
  <AppLayout>
    <div class="page">
      <!-- ページタイトル -->
      <h2 class="page-title">
        <img src="@/image/icon-token.svg" class="page-title-icon" alt="登録トークン管理" />
        エージェント登録トークン管理
      </h2>

      <!-- 説明エリア -->
      <div class="info-box">
        <p>
          エージェントアプリを新しいPCにインストールする際に必要なトークンを発行します。<br />
          トークンは<strong>発行から24時間</strong>有効で、<strong>1回限り</strong>使用できます。<br />
        </p>
      </div>

      <!-- トークン発行エリア -->
      <div class="issue-card">
        <h3 class="section-title">新規トークン発行</h3>
        <div class="issue-form">
          <input
            v-model="noteInput"
            type="text"
            class="note-input"
            placeholder="メモ（任意）: 対象PC名や用途など"
            maxlength="200"
          />
          <button class="btn-issue" :disabled="isIssuing" @click="handleIssue">
            {{ isIssuing ? '発行中...' : 'トークンを発行' }}
          </button>
        </div>

        <!-- 発行直後のトークン表示（この画面でのみ表示） -->
        <div v-if="issuedToken" class="issued-token-box">
          <div class="issued-label">
            <img src="@/image/icon-warning.svg" class="warn-icon" alt="注意" />
            このトークンはこの画面でのみ表示されます。コピーして安全な場所に保存してください。
          </div>
          <div class="token-display">
            <code class="token-text">{{ issuedToken.token }}</code>
            <button class="btn-copy" @click="copyToken(issuedToken.token)">
              {{ copied ? 'コピー済み' : 'コピー' }}
            </button>
          </div>
          <div class="token-meta">
            有効期限: {{ formatDate(issuedToken.expiresAt) }} &nbsp;|&nbsp;
            メモ: {{ issuedToken.note ?? '(なし)' }}
          </div>
        </div>

        <!-- エラーメッセージ -->
        <p v-if="issueError" class="error-msg">{{ issueError }}</p>
      </div>

      <!-- トークン一覧 -->
      <div class="list-card">
        <div class="list-header">
          <h3 class="section-title">発行済みトークン一覧</h3>
          <button class="btn-reload" @click="loadTokens">再読み込み</button>
        </div>

        <div v-if="isLoading" class="loading">読み込み中...</div>
        <div v-else-if="tokens.length === 0" class="empty">発行済みトークンはありません</div>
        <table v-else class="token-table">
          <thead>
            <tr>
              <th>状態</th>
              <th>トークン（先頭8文字）</th>
              <th>有効期限</th>
              <th>使用エージェント番号</th>
              <th>使用日時</th>
              <th>メモ</th>
              <th>発行日時</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in tokens" :key="t.id">
              <td>
                <span class="status-badge" :class="statusClass(t.status)">
                  {{ statusLabel(t.status) }}
                </span>
              </td>
              <!-- トークンは先頭8文字のみ表示（セキュリティ上の考慮） -->
              <td class="token-preview">{{ t.token.substring(0, 8) }}...</td>
              <td>{{ formatDate(t.expiresAt) }}</td>
              <td>{{ t.usedByAgentNumber ?? '-' }}</td>
              <td>{{ t.usedAt ? formatDate(t.usedAt) : '-' }}</td>
              <td>{{ t.note ?? '-' }}</td>
              <td>{{ formatDate(t.createdAt) }}</td>
              <td>
                <!-- ADMIN のみ削除可能 -->
                <button
                  v-if="authStore.isAdmin"
                  class="btn-delete"
                  @click="handleDelete(t.id)"
                >
                  削除
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import AppLayout from '@/components/common/AppLayout.vue'
import { agentTokensApi } from '@/api/agentTokens'
import { useAuthStore } from '@/stores/auth'
import type { EnrollmentToken } from '@/types'

const authStore = useAuthStore()

/** トークン一覧 */
const tokens = ref<EnrollmentToken[]>([])
/** 一覧読み込み中フラグ */
const isLoading = ref(false)
/** トークン発行中フラグ */
const isIssuing = ref(false)
/** 発行メモ入力値 */
const noteInput = ref('')
/** 発行直後に表示するトークン（発行成功時のみ設定） */
const issuedToken = ref<EnrollmentToken | null>(null)
/** コピー完了フラグ（UIフィードバック用） */
const copied = ref(false)
/** 発行エラーメッセージ */
const issueError = ref('')

// =====================================================
// ライフサイクル
// =====================================================
onMounted(loadTokens)

// =====================================================
// データ取得
// =====================================================

/** トークン一覧を読み込む */
async function loadTokens(): Promise<void> {
  isLoading.value = true
  try {
    const res = await agentTokensApi.findAll()
    tokens.value = res.data
  } catch {
    tokens.value = []
  } finally {
    isLoading.value = false
  }
}

// =====================================================
// トークン発行
// =====================================================

/** トークンを発行する */
async function handleIssue(): Promise<void> {
  isIssuing.value = true
  issuedToken.value = null
  issueError.value = ''
  copied.value = false
  try {
    const res = await agentTokensApi.issue({ note: noteInput.value || undefined })
    issuedToken.value = res.data
    noteInput.value = ''
    // 一覧を再読み込みして最新状態を反映する
    await loadTokens()
  } catch (e: unknown) {
    issueError.value = e instanceof Error ? e.message : 'トークンの発行に失敗しました'
  } finally {
    isIssuing.value = false
  }
}

// =====================================================
// トークン削除
// =====================================================

/** 指定IDのトークンを削除する */
async function handleDelete(id: number): Promise<void> {
  if (!confirm('このトークンを削除しますか？')) return
  try {
    await agentTokensApi.delete(id)
    await loadTokens()
  } catch {
    alert('削除に失敗しました')
  }
}

// =====================================================
// クリップボードコピー
// =====================================================

/** トークン文字列をクリップボードにコピーする */
async function copyToken(token: string): Promise<void> {
  try {
    await navigator.clipboard.writeText(token)
    copied.value = true
    // 3秒後にコピー済み表示をリセットする
    setTimeout(() => { copied.value = false }, 3000)
  } catch {
    alert('コピーに失敗しました。手動でコピーしてください。')
  }
}

// =====================================================
// ユーティリティ
// =====================================================

/** 日付文字列を「YYYY/MM/DD HH:mm」形式に変換する */
function formatDate(dateStr: string): string {
  const d = new Date(dateStr)
  const yyyy  = d.getFullYear()
  const mm    = String(d.getMonth() + 1).padStart(2, '0')
  const dd    = String(d.getDate()).padStart(2, '0')
  const hh    = String(d.getHours()).padStart(2, '0')
  const min   = String(d.getMinutes()).padStart(2, '0')
  return `${yyyy}/${mm}/${dd} ${hh}:${min}`
}

/** トークン状態に対応するラベルを返す */
function statusLabel(status: string): string {
  switch (status) {
    case 'UNUSED':  return '未使用'
    case 'USED':    return '使用済み'
    case 'EXPIRED': return '期限切れ'
    default:        return status
  }
}

/** トークン状態に対応するCSSクラスを返す */
function statusClass(status: string): string {
  switch (status) {
    case 'UNUSED':  return 'badge-green'
    case 'USED':    return 'badge-gray'
    case 'EXPIRED': return 'badge-red'
    default:        return ''
  }
}
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 24px; }
.page-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}
.page-title-icon { width: 24px; height: 24px; }

/* 説明ボックス */
.info-box {
  background: #eff6ff;
  border: 1px solid #bfdbfe;
  border-radius: 8px;
  padding: 16px 20px;
  font-size: 14px;
  color: #1e3a5f;
  line-height: 1.7;
}
.info-box code {
  background: #dbeafe;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
}

/* 発行カード */
.issue-card, .list-card {
  background: white;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.07);
}
.section-title { font-size: 15px; font-weight: 700; color: #1a1a2e; margin: 0 0 16px; }

/* 発行フォーム */
.issue-form { display: flex; gap: 12px; align-items: center; }
.note-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}
.btn-issue {
  padding: 8px 20px;
  background: #6366f1;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  white-space: nowrap;
}
.btn-issue:disabled { background: #a5b4fc; cursor: not-allowed; }
.btn-issue:not(:disabled):hover { background: #4f46e5; }

/* 発行済みトークン表示 */
.issued-token-box {
  margin-top: 16px;
  border: 2px solid #fbbf24;
  border-radius: 8px;
  padding: 16px;
  background: #fffbeb;
}
.issued-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  font-weight: 600;
  color: #92400e;
  margin-bottom: 12px;
}
.warn-icon { width: 16px; height: 16px; }
.token-display { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.token-text {
  flex: 1;
  background: #1a1a2e;
  color: #a5f3fc;
  padding: 10px 14px;
  border-radius: 6px;
  font-size: 13px;
  word-break: break-all;
  font-family: monospace;
}
.btn-copy {
  padding: 8px 16px;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  white-space: nowrap;
}
.btn-copy:hover { background: #059669; }
.token-meta { font-size: 12px; color: #78350f; }

/* エラーメッセージ */
.error-msg { color: #ef4444; font-size: 14px; margin-top: 8px; }

/* 一覧 */
.list-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.btn-reload {
  padding: 6px 14px;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
}
.btn-reload:hover { background: #f3f4f6; }

.loading, .empty { text-align: center; color: #6b7280; padding: 24px; font-size: 14px; }

.token-table { width: 100%; border-collapse: collapse; font-size: 13px; }
.token-table th {
  background: #f9fafb;
  padding: 10px 12px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
  white-space: nowrap;
}
.token-table td {
  padding: 10px 12px;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
  vertical-align: middle;
}
.token-table tr:hover td { background: #fafafa; }
.token-preview { font-family: monospace; color: #6366f1; }

/* 状態バッジ */
.status-badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}
.badge-green  { background: #d1fae5; color: #065f46; }
.badge-gray   { background: #f3f4f6; color: #374151; }
.badge-red    { background: #fee2e2; color: #991b1b; }

/* 削除ボタン */
.btn-delete {
  padding: 4px 12px;
  background: white;
  border: 1px solid #fca5a5;
  color: #ef4444;
  border-radius: 6px;
  font-size: 12px;
  cursor: pointer;
}
.btn-delete:hover { background: #fee2e2; }
</style>
