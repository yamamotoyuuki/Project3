<template>
  <AppLayout>
    <div class="page">
      <!-- 戻るボタン -->
      <button class="back-btn" @click="router.back()">← 一覧に戻る</button>

      <div v-if="loading" class="loading">読み込み中…</div>
      <div v-else-if="error" class="error-msg">{{ error }}</div>

      <template v-else-if="asset">
        <!-- ページヘッダー -->
        <div class="page-header">
          <div>
            <h2 class="page-title">{{ asset.deviceName }}</h2>
            <p class="asset-number mono">{{ asset.assetNumber }}</p>
          </div>
          <div class="header-actions">
            <StatusBadge :status="asset.status" />
            <button v-if="authStore.isItStaff" class="btn btn-primary" @click="openEdit">
              ✏️ 編集
            </button>
            <button
              v-if="authStore.isAdmin"
              class="btn btn-danger"
              @click="confirmDelete"
            >
              🗑 削除
            </button>
          </div>
        </div>

        <!-- 基本情報 -->
        <div class="card">
          <h3 class="section-title">基本情報</h3>
          <dl class="info-grid">
            <dt>取得区分</dt>
            <dd>
              <span class="acq-badge" :class="asset.acquisitionType === 'RENTAL' ? 'rental' : 'purchase'">
                {{ asset.acquisitionType === 'RENTAL' ? 'レンタル' : '購入' }}
              </span>
            </dd>
            <dt>メーカー</dt>
            <dd>{{ asset.maker || '—' }}</dd>
            <dt>型番</dt>
            <dd>{{ asset.modelNumber || '—' }}</dd>
            <dt>シリアル番号</dt>
            <dd class="mono">{{ asset.serialNumber || '—' }}</dd>
            <dt>ホスト名</dt>
            <dd class="mono">{{ asset.hostname || '—' }}</dd>
            <dt>場所</dt>
            <dd>{{ asset.location || '—' }}</dd>
            <dt>使用者</dt>
            <dd>{{ asset.assignedEmployeeName || '—' }}</dd>
            <dt>エージェント最終確認</dt>
            <dd>{{ formatDate(asset.agentLastSeen) }}</dd>
            <dt>登録日時</dt>
            <dd>{{ formatDate(asset.createdAt) }}</dd>
            <dt>更新日時</dt>
            <dd>{{ formatDate(asset.updatedAt) }}</dd>
          </dl>
        </div>

        <!-- 備考 -->
        <div v-if="asset.note" class="card">
          <h3 class="section-title">備考</h3>
          <p class="note-text">{{ asset.note }}</p>
        </div>
      </template>
    </div>

    <!-- 編集モーダル -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h3 class="modal-title">PC編集</h3>
        <div class="form-grid">
          <div class="form-group full">
            <label>端末名 <span class="required">*</span></label>
            <input v-model="form.deviceName" class="input" />
          </div>
          <div class="form-group">
            <label>取得区分</label>
            <select v-model="form.acquisitionType" class="select">
              <option value="PURCHASE">購入</option>
              <option value="RENTAL">レンタル</option>
            </select>
          </div>
          <div class="form-group">
            <label>ステータス</label>
            <select v-model="form.status" class="select">
              <option value="IN_USE">使用中</option>
              <option value="IN_STORAGE">保管中</option>
              <option value="IN_REPAIR">修理中</option>
              <option value="DISPOSED">廃棄済</option>
              <option value="RETURNED">返却済</option>
            </select>
          </div>
          <div class="form-group">
            <label>メーカー</label>
            <input v-model="form.maker" class="input" />
          </div>
          <div class="form-group">
            <label>型番</label>
            <input v-model="form.modelNumber" class="input" />
          </div>
          <div class="form-group">
            <label>シリアル番号</label>
            <input v-model="form.serialNumber" class="input" />
          </div>
          <div class="form-group">
            <label>ホスト名</label>
            <input v-model="form.hostname" class="input" />
          </div>
          <div class="form-group">
            <label>場所</label>
            <input v-model="form.location" class="input" />
          </div>
          <div class="form-group">
            <label>使用者</label>
            <select v-model="form.assignedEmployeeId" class="select">
              <option :value="null">（なし）</option>
              <option v-for="e in activeEmployees" :key="e.id" :value="e.id">
                {{ e.employeeCode }} {{ e.fullName }}
              </option>
            </select>
          </div>
          <div class="form-group full">
            <label>備考</label>
            <textarea v-model="form.note" class="textarea" rows="3" />
          </div>
        </div>
        <p v-if="formError" class="form-error">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="closeModal">キャンセル</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveAsset">
            {{ saving ? '保存中…' : '保存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 削除確認ダイアログ -->
    <div v-if="showDeleteConfirm" class="modal-overlay" @click.self="showDeleteConfirm = false">
      <div class="modal modal-sm">
        <h3 class="modal-title">削除確認</h3>
        <p>「{{ asset?.deviceName }}」を削除します。この操作は取り消せません。</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showDeleteConfirm = false">キャンセル</button>
          <button class="btn btn-danger" :disabled="deleting" @click="deleteAsset">
            {{ deleting ? '削除中…' : '削除する' }}
          </button>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { assetsApi } from '@/api/assets'
import { employeesApi } from '@/api/employees'
import AppLayout from '@/components/AppLayout.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import type { PcAsset, Employee, AcquisitionType, PcStatus } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const asset = ref<PcAsset | null>(null)
const loading = ref(false)
const error = ref('')

async function loadAsset() {
  loading.value = true
  error.value = ''
  try {
    const res = await assetsApi.findById(Number(route.params.id))
    asset.value = res.data
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? 'データの取得に失敗しました'
  } finally {
    loading.value = false
  }
}

function formatDate(v: string | null): string {
  if (!v) return '—'
  return new Date(v).toLocaleString('ja-JP', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

// ---- 社員プルダウン ----
const activeEmployees = ref<Employee[]>([])
async function loadActiveEmployees() {
  try {
    const res = await employeesApi.findActiveList()
    activeEmployees.value = res.data
  } catch { /* ignore */ }
}

// ---- 編集モーダル ----
const showModal = ref(false)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  deviceName: '',
  acquisitionType: 'PURCHASE' as AcquisitionType,
  status: 'IN_STORAGE' as PcStatus,
  maker: '',
  modelNumber: '',
  serialNumber: '',
  hostname: '',
  location: '',
  assignedEmployeeId: null as number | null,
  note: '',
})

function openEdit() {
  if (!asset.value) return
  Object.assign(form, {
    deviceName: asset.value.deviceName,
    acquisitionType: asset.value.acquisitionType,
    status: asset.value.status,
    maker: asset.value.maker ?? '',
    modelNumber: asset.value.modelNumber ?? '',
    serialNumber: asset.value.serialNumber ?? '',
    hostname: asset.value.hostname ?? '',
    location: asset.value.location ?? '',
    assignedEmployeeId: asset.value.assignedEmployeeId,
    note: asset.value.note ?? '',
  })
  formError.value = ''
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function saveAsset() {
  if (!form.deviceName) { formError.value = '端末名は必須です'; return }
  saving.value = true
  try {
    await assetsApi.update(Number(route.params.id), {
      deviceName: form.deviceName,
      acquisitionType: form.acquisitionType,
      status: form.status,
      maker: form.maker || undefined,
      modelNumber: form.modelNumber || undefined,
      serialNumber: form.serialNumber || undefined,
      hostname: form.hostname || undefined,
      location: form.location || undefined,
      assignedEmployeeId: form.assignedEmployeeId,
      note: form.note || undefined,
    })
    closeModal()
    loadAsset()
  } catch (e: any) {
    formError.value = e?.response?.data?.message ?? '保存に失敗しました'
  } finally {
    saving.value = false
  }
}

// ---- 削除 ----
const showDeleteConfirm = ref(false)
const deleting = ref(false)

function confirmDelete() { showDeleteConfirm.value = true }

async function deleteAsset() {
  deleting.value = true
  try {
    await assetsApi.delete(Number(route.params.id))
    router.push('/assets')
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? '削除に失敗しました'
    showDeleteConfirm.value = false
  } finally {
    deleting.value = false
  }
}

onMounted(() => {
  loadAsset()
  loadActiveEmployees()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 20px; }
.back-btn {
  background: none;
  border: none;
  color: #6366f1;
  cursor: pointer;
  font-size: 14px;
  padding: 0;
  text-decoration: underline;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}
.page-title { font-size: 22px; font-weight: 700; color: #1a1a2e; margin: 0; }
.asset-number { font-size: 13px; color: #6b7280; margin: 4px 0 0; }
.header-actions { display: flex; align-items: center; gap: 10px; }

.card {
  background: white;
  border-radius: 10px;
  padding: 24px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.07);
}
.section-title { font-size: 15px; font-weight: 700; color: #1a1a2e; margin: 0 0 16px; }

.info-grid {
  display: grid;
  grid-template-columns: 150px 1fr;
  gap: 10px 16px;
  margin: 0;
}
.info-grid dt { font-size: 12px; font-weight: 600; color: #6b7280; align-self: center; }
.info-grid dd { font-size: 14px; color: #1a1a2e; margin: 0; }

.note-text { font-size: 14px; color: #374151; white-space: pre-wrap; }
.mono { font-family: monospace; }

.loading { padding: 40px; text-align: center; color: #9ca3af; }
.error-msg { color: #ef4444; padding: 16px; }

.acq-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}
.purchase { background: #e0f2fe; color: #0369a1; }
.rental   { background: #fdf4ff; color: #7e22ce; }

.btn {
  height: 36px; padding: 0 16px; border-radius: 6px; font-size: 13px;
  cursor: pointer; border: none; font-weight: 500; transition: opacity 0.15s;
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: #6366f1; color: white; }
.btn-primary:hover { background: #4f46e5; }
.btn-danger { background: #ef4444; color: white; }
.btn-danger:hover { background: #dc2626; }
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }

/* Modal */
.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal {
  background: white; border-radius: 12px; padding: 28px;
  width: 680px; max-width: 95vw; max-height: 90vh; overflow-y: auto;
}
.modal-sm { width: 420px; }
.modal-title { font-size: 18px; font-weight: 700; margin: 0 0 20px; color: #1a1a2e; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group.full { grid-column: 1 / -1; }
.form-group label { font-size: 12px; font-weight: 600; color: #374151; }
.input, .select {
  height: 36px; border: 1px solid #d1d5db; border-radius: 6px;
  padding: 0 10px; font-size: 13px; outline: none;
}
.input:focus, .select:focus { border-color: #6366f1; }
.textarea {
  border: 1px solid #d1d5db; border-radius: 6px;
  padding: 8px 10px; font-size: 13px; resize: vertical; outline: none;
}
.required { color: #ef4444; }
.form-error { color: #ef4444; font-size: 13px; margin-top: 8px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
</style>
