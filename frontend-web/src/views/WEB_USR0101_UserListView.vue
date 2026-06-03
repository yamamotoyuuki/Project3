<!--
  機能ID: WEB_USR0101
  views/WEB_USR0101_UserListView.vue
  -----------------------------------------------
  ユーザー一覧画面コンポーネント
  -----------------------------------------------
-->
<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <h2 class="page-title"><img src="@/image/icon-settings.svg" class="page-title-icon" alt="ユーザー管理" />ユーザー管理</h2>
        <button class="btn btn-primary" @click="openCreate">＋ 追加</button>
      </div>

      <div class="card table-card">
        <div v-if="loading" class="loading">読み込み中…</div>
        <div v-else-if="error" class="error-msg">{{ error }}</div>
        <template v-else>
          <table class="table">
            <thead>
              <tr>
                <th>ユーザー名</th>
                <th>表示名</th>
                <th>ロール</th>
                <th>メール</th>
                <th>最終ログイン</th>
                <th>ステータス</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="users.length === 0"><td colspan="7" class="empty">ユーザーがありません</td></tr>
              <tr v-for="u in users" :key="u.id">
                <td class="mono">{{ u.username }}</td>
                <td>{{ u.displayName }}</td>
                <td><span class="role-badge" :class="u.role.toLowerCase()">{{ roleLabel(u.role) }}</span></td>
                <td class="small-text">{{ u.email || '—' }}</td>
                <td class="small-text">{{ formatDate(u.lastLoginAt) }}</td>
                <td>
                  <span class="active-badge" :class="u.isActive ? 'active' : 'inactive'">
                    {{ u.isActive ? '有効' : '無効' }}
                  </span>
                </td>
                <td>
                  <button class="btn-icon" @click="openEdit(u)"><img src="@/image/icon-edit.svg" alt="編集" /></button>
                </td>
              </tr>
            </tbody>
          </table>
        </template>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h3 class="modal-title">{{ editTarget ? 'ユーザー編集' : 'ユーザー追加' }}</h3>
        <div class="form-grid">
          <div v-if="!editTarget" class="form-group full">
            <label>ユーザー名 <span class="required">*</span></label>
            <input v-model="form.username" class="input" />
          </div>
          <div class="form-group" :class="{ full: !editTarget }">
            <label>{{ editTarget ? 'パスワード変更（空白=変更なし）' : 'パスワード' }} <span v-if="!editTarget" class="required">*</span></label>
            <input v-model="form.password" type="password" class="input" />
          </div>
          <div class="form-group">
            <label>表示名 <span class="required">*</span></label>
            <input v-model="form.displayName" class="input" />
          </div>
          <div class="form-group">
            <label>ロール <span class="required">*</span></label>
            <select v-model="form.role" class="select">
              <option value="ADMIN">管理者</option>
              <option value="IT_STAFF">IT担当</option>
              <option value="VIEWER">閲覧者</option>
            </select>
          </div>
          <div class="form-group full">
            <label>メールアドレス</label>
            <input v-model="form.email" type="email" class="input" />
          </div>
          <div v-if="editTarget" class="form-group">
            <label>ステータス</label>
            <select v-model="form.isActive" class="select">
              <option :value="true">有効</option>
              <option :value="false">無効</option>
            </select>
          </div>
        </div>
        <p v-if="formError" class="form-error">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="closeModal">キャンセル</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveUser">
            {{ saving ? '保存中…' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { usersApi } from '@/api/users'
import AppLayout from '@/components/AppLayout.vue'
import type { SystemUser } from '@/types'

const users = ref<SystemUser[]>([])
const loading = ref(false)
const error = ref('')

async function loadUsers() {
  loading.value = true; error.value = ''
  try { users.value = (await usersApi.findAll()).data }
  catch (e: any) { error.value = e?.response?.data?.message ?? '取得失敗' }
  finally { loading.value = false }
}

function formatDate(v: string | null): string {
  if (!v) return '—'
  return new Date(v).toLocaleString('ja-JP', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}
function roleLabel(r: string): string {
  return r === 'ADMIN' ? '管理者' : r === 'IT_STAFF' ? 'IT担当' : '閲覧者'
}

const showModal = ref(false)
const saving = ref(false)
const formError = ref('')
const editTarget = ref<SystemUser | null>(null)
const form = reactive({ username: '', password: '', displayName: '', role: 'VIEWER' as 'ADMIN' | 'IT_STAFF' | 'VIEWER', email: '', isActive: true })

function openCreate() {
  editTarget.value = null
  Object.assign(form, { username: '', password: '', displayName: '', role: 'VIEWER', email: '', isActive: true })
  formError.value = ''; showModal.value = true
}
function openEdit(u: SystemUser) {
  editTarget.value = u
  Object.assign(form, { displayName: u.displayName, role: u.role, email: u.email ?? '', isActive: u.isActive, password: '' })
  formError.value = ''; showModal.value = true
}
function closeModal() { showModal.value = false }

async function saveUser() {
  formError.value = ''
  if (!form.displayName) { formError.value = '表示名は必須です'; return }
  saving.value = true
  try {
    if (editTarget.value) {
      await usersApi.update(editTarget.value.id, {
        displayName: form.displayName, role: form.role, email: form.email || undefined,
        isActive: form.isActive, password: form.password || undefined,
      })
    } else {
      if (!form.username) { formError.value = 'ユーザー名は必須です'; return }
      if (!form.password) { formError.value = 'パスワードは必須です'; return }
      await usersApi.create({ username: form.username, password: form.password, displayName: form.displayName, role: form.role, email: form.email || undefined })
    }
    closeModal(); loadUsers()
  } catch (e: any) { formError.value = e?.response?.data?.message ?? '保存失敗' }
  finally { saving.value = false }
}

onMounted(loadUsers)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-size: 20px; font-weight: 700; color: #1a1a2e; margin: 0; }
.card { background: white; border-radius: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.07); }
.table-card { padding: 16px; }
.table { width: 100%; border-collapse: collapse; font-size: 13px; }
.table th { background: #f9fafb; padding: 10px 12px; text-align: left; font-weight: 600; color: #374151; border-bottom: 1px solid #e5e7eb; }
.table td { padding: 10px 12px; border-bottom: 1px solid #f3f4f6; }
.empty { text-align: center; color: #9ca3af; padding: 40px; }
.mono { font-family: monospace; }
.small-text { font-size: 12px; color: #6b7280; }
.role-badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 11px; font-weight: 700; }
.role-badge.admin    { background: #fef3c7; color: #92400e; }
.role-badge.it_staff { background: #dbeafe; color: #1e40af; }
.role-badge.viewer   { background: #f3f4f6; color: #6b7280; }
.active-badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 11px; font-weight: 600; }
.active   { background: #dcfce7; color: #166534; }
.inactive { background: #fee2e2; color: #991b1b; }
.loading { padding: 40px; text-align: center; color: #9ca3af; }
.error-msg { padding: 16px; color: #ef4444; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 15px; padding: 4px; border-radius: 4px; }
.btn-icon:hover { background: #f3f4f6; }
.btn { height: 36px; padding: 0 16px; border-radius: 6px; font-size: 13px; cursor: pointer; border: none; font-weight: 500; }
.btn:disabled { opacity: 0.5; }
.btn-primary { background: #6366f1; color: white; }
.btn-primary:hover { background: #4f46e5; }
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: white; border-radius: 12px; padding: 28px; width: 520px; max-width: 95vw; max-height: 90vh; overflow-y: auto; }
.modal-title { font-size: 18px; font-weight: 700; margin: 0 0 20px; color: #1a1a2e; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group.full { grid-column: 1 / -1; }
.form-group label { font-size: 12px; font-weight: 600; color: #374151; }
.input, .select { height: 36px; border: 1px solid #d1d5db; border-radius: 6px; padding: 0 10px; font-size: 13px; outline: none; }
.input:focus, .select:focus { border-color: #6366f1; }
.required { color: #ef4444; }
.form-error { color: #ef4444; font-size: 13px; margin-top: 8px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
</style>
