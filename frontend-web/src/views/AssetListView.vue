<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <h2 class="page-title">💻 PC一覧</h2>
        <div class="header-actions">
          <!-- CSV / Excel エクスポートボタン -->
          <a class="btn btn-ghost" :href="exportUrl('assets.csv')" download>
            📥 CSV
          </a>
          <a class="btn btn-ghost" :href="exportUrl('assets.xlsx')" download>
            📊 Excel
          </a>
          <button v-if="authStore.isItStaff" class="btn btn-primary" @click="openCreate">
            ＋ PC登録
          </button>
        </div>
      </div>

      <!-- 検索バー -->
      <div class="search-bar card">
        <input
          v-model="searchForm.keyword"
          class="input"
          placeholder="資産番号 / 端末名 / シリアル / ホスト名"
          @keyup.enter="doSearch"
        />
        <select v-model="searchForm.status" class="select">
          <option value="">すべてのステータス</option>
          <option value="IN_USE">使用中</option>
          <option value="IN_STORAGE">保管中</option>
          <option value="IN_REPAIR">修理中</option>
          <option value="DISPOSED">廃棄済</option>
          <option value="RETURNED">返却済</option>
        </select>
        <select v-model="searchForm.acquisitionType" class="select">
          <option value="">取得区分 : すべて</option>
          <option value="PURCHASE">購入</option>
          <option value="RENTAL">レンタル</option>
        </select>
        <button class="btn btn-secondary" @click="doSearch">🔍 検索</button>
        <button class="btn btn-ghost" @click="resetSearch">リセット</button>
      </div>

      <!-- テーブル -->
      <div class="card table-card">
        <div v-if="loading" class="loading">読み込み中…</div>
        <div v-else-if="error" class="error-msg">{{ error }}</div>
        <template v-else>
          <table class="table">
            <thead>
              <tr>
                <th>資産番号</th>
                <th>端末名</th>
                <th>メーカー / 型番</th>
                <th>ステータス</th>
                <th>取得区分</th>
                <th>使用者</th>
                <th>場所</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="assets.length === 0">
                <td colspan="8" class="empty">データがありません</td>
              </tr>
              <tr
                v-for="a in assets"
                :key="a.id"
                class="row-hover"
                @click="goDetail(a.id)"
              >
                <td class="mono">{{ a.assetNumber }}</td>
                <td>{{ a.deviceName }}</td>
                <td class="small-text">
                  {{ [a.maker, a.modelNumber].filter(Boolean).join(' / ') || '—' }}
                </td>
                <td><StatusBadge :status="a.status" /></td>
                <td>
                  <span class="acq-badge" :class="a.acquisitionType === 'RENTAL' ? 'rental' : 'purchase'">
                    {{ a.acquisitionType === 'RENTAL' ? 'レンタル' : '購入' }}
                  </span>
                </td>
                <td>{{ a.assignedEmployeeName || '—' }}</td>
                <td class="small-text">{{ a.location || '—' }}</td>
                <td @click.stop>
                  <button
                    v-if="authStore.isItStaff"
                    class="btn-icon"
                    title="編集"
                    @click="openEdit(a)"
                  >✏️</button>
                </td>
              </tr>
            </tbody>
          </table>

          <AppPagination
            v-model="currentPage"
            :total-pages="totalPages"
            :total-elements="totalElements"
            :size="pageSize"
          />
        </template>
      </div>
    </div>

    <!-- 登録 / 編集モーダル -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h3 class="modal-title">{{ editTarget ? 'PC編集' : 'PC登録' }}</h3>

        <div class="form-grid">
          <div v-if="!editTarget" class="form-group full">
            <label>資産番号 <span class="required">*</span></label>
            <input v-model="form.assetNumber" class="input" placeholder="例: PC-2024-001" />
          </div>
          <div class="form-group full">
            <label>端末名 <span class="required">*</span></label>
            <input v-model="form.deviceName" class="input" placeholder="例: ThinkPad X1 Carbon" />
          </div>
          <div class="form-group">
            <label>取得区分 <span class="required">*</span></label>
            <select v-model="form.acquisitionType" class="select">
              <option value="PURCHASE">購入</option>
              <option value="RENTAL">レンタル</option>
            </select>
          </div>
          <div class="form-group">
            <label>ステータス <span class="required">*</span></label>
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
            <input v-model="form.maker" class="input" placeholder="例: Lenovo" />
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
            <input v-model="form.location" class="input" placeholder="例: 東京本社 3F" />
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
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { assetsApi } from '@/api/assets'
import { employeesApi } from '@/api/employees'
import AppLayout from '@/components/AppLayout.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import AppPagination from '@/components/AppPagination.vue'
import type { PcAsset, Employee, AcquisitionType, PcStatus } from '@/types'

const authStore = useAuthStore()
const router = useRouter()

/**
 * エクスポート URL を生成する
 * JWT トークンをクエリパラメータとして付与し、認証済みダウンロードを実現する。
 * @param filename - エクスポートファイル名（例: "assets.csv"）
 */
function exportUrl(filename: string): string {
  const token = localStorage.getItem('token') ?? ''
  return `/api/v1/export/${filename}?token=${token}`
}

// ---- 一覧 ----
const assets = ref<PcAsset[]>([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const pageSize = 20
const totalPages = ref(0)
const totalElements = ref(0)

const searchForm = reactive({
  keyword: '',
  status: '' as PcStatus | '',
  acquisitionType: '' as AcquisitionType | '',
})

async function loadAssets() {
  loading.value = true
  error.value = ''
  try {
    const res = await assetsApi.findAll({
      page: currentPage.value,
      size: pageSize,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined,
      acquisitionType: searchForm.acquisitionType || undefined,
    })
    assets.value = res.data.content
    totalPages.value = res.data.totalPages
    totalElements.value = res.data.totalElements
  } catch (e: any) {
    error.value = e?.response?.data?.message ?? 'データの取得に失敗しました'
  } finally {
    loading.value = false
  }
}

function doSearch() {
  currentPage.value = 0
  loadAssets()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.status = ''
  searchForm.acquisitionType = ''
  currentPage.value = 0
  loadAssets()
}

watch(currentPage, loadAssets)

function goDetail(id: number) {
  router.push(`/assets/${id}`)
}

// ---- 社員プルダウン ----
const activeEmployees = ref<Employee[]>([])

async function loadActiveEmployees() {
  try {
    const res = await employeesApi.findActiveList()
    activeEmployees.value = res.data
  } catch { /* ignore */ }
}

// ---- モーダル ----
const showModal = ref(false)
const saving = ref(false)
const formError = ref('')
const editTarget = ref<PcAsset | null>(null)

const form = reactive({
  assetNumber: '',
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

function openCreate() {
  editTarget.value = null
  Object.assign(form, {
    assetNumber: '', deviceName: '', acquisitionType: 'PURCHASE',
    status: 'IN_STORAGE', maker: '', modelNumber: '', serialNumber: '',
    hostname: '', location: '', assignedEmployeeId: null, note: '',
  })
  formError.value = ''
  showModal.value = true
}

function openEdit(a: PcAsset) {
  editTarget.value = a
  Object.assign(form, {
    deviceName: a.deviceName,
    acquisitionType: a.acquisitionType,
    status: a.status,
    maker: a.maker ?? '',
    modelNumber: a.modelNumber ?? '',
    serialNumber: a.serialNumber ?? '',
    hostname: a.hostname ?? '',
    location: a.location ?? '',
    assignedEmployeeId: a.assignedEmployeeId,
    note: a.note ?? '',
  })
  formError.value = ''
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function saveAsset() {
  formError.value = ''
  if (!form.deviceName) { formError.value = '端末名は必須です'; return }
  saving.value = true
  try {
    if (editTarget.value) {
      await assetsApi.update(editTarget.value.id, {
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
    } else {
      if (!form.assetNumber) { formError.value = '資産番号は必須です'; return }
      await assetsApi.create({
        assetNumber: form.assetNumber,
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
    }
    closeModal()
    loadAssets()
  } catch (e: any) {
    formError.value = e?.response?.data?.message ?? '保存に失敗しました'
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadAssets()
  loadActiveEmployees()
})
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
/* ヘッダー右側のボタングループ */
.header-actions { display: flex; gap: 8px; align-items: center; }
.page-title  { font-size: 20px; font-weight: 700; color: #1a1a2e; margin: 0; }

.search-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  padding: 14px 16px;
}

.card {
  background: white;
  border-radius: 10px;
  box-shadow: 0 1px 4px rgba(0,0,0,0.07);
}
.table-card { padding: 16px; }

.input, .select {
  height: 36px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 13px;
  outline: none;
}
.input { min-width: 220px; }
.input:focus, .select:focus { border-color: #6366f1; }

.btn {
  height: 36px;
  padding: 0 16px;
  border-radius: 6px;
  font-size: 13px;
  cursor: pointer;
  border: none;
  font-weight: 500;
  transition: opacity 0.15s;
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary  { background: #6366f1; color: white; }
.btn-primary:hover  { background: #4f46e5; }
.btn-secondary { background: #f3f4f6; color: #374151; border: 1px solid #d1d5db; }
.btn-secondary:hover { background: #e5e7eb; }
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }
.btn-ghost:hover { background: #f9fafb; }

.table { width: 100%; border-collapse: collapse; font-size: 13px; }
.table th {
  background: #f9fafb;
  padding: 10px 12px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}
.table td {
  padding: 10px 12px;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
}
.row-hover { cursor: pointer; transition: background 0.1s; }
.row-hover:hover { background: #f9fafb; }
.empty { text-align: center; color: #9ca3af; padding: 40px; }
.mono { font-family: monospace; }
.small-text { font-size: 12px; color: #6b7280; }

.acq-badge {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}
.purchase { background: #e0f2fe; color: #0369a1; }
.rental   { background: #fdf4ff; color: #7e22ce; }

.loading { padding: 40px; text-align: center; color: #9ca3af; }
.error-msg { padding: 16px; color: #ef4444; }

.btn-icon {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 4px;
  border-radius: 4px;
  transition: background 0.1s;
}
.btn-icon:hover { background: #f3f4f6; }

/* ---- Modal ---- */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}
.modal {
  background: white;
  border-radius: 12px;
  padding: 28px;
  width: 680px;
  max-width: 95vw;
  max-height: 90vh;
  overflow-y: auto;
}
.modal-title { font-size: 18px; font-weight: 700; margin: 0 0 20px; color: #1a1a2e; }

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 14px;
}
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group.full { grid-column: 1 / -1; }
.form-group label { font-size: 12px; font-weight: 600; color: #374151; }
.form-group .input,
.form-group .select { height: 36px; }
.textarea {
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 8px 10px;
  font-size: 13px;
  resize: vertical;
  outline: none;
}
.textarea:focus { border-color: #6366f1; }
.required { color: #ef4444; }

.form-error { color: #ef4444; font-size: 13px; margin-top: 8px; }

.modal-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 24px;
}
</style>
