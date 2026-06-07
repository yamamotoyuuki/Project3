<!--
  機能ID: WEB_LON0101
  views/WEB_LON0101_LoanListView.vue
  -----------------------------------------------
  貸出一覧画面コンポーネント
  -----------------------------------------------
-->
<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <h2 class="page-title">
          <img src="@/image/icon-loan.svg" class="page-title-icon" alt="貸出管理" />
          貸出管理
        </h2>
        <button v-if="authStore.isItStaff" class="btn btn-primary" @click="openCreate">
          ＋ 貸出登録
        </button>
      </div>

      <!-- 検索バー -->
      <div class="search-bar card">
        <input v-model="searchForm.keyword" class="input" placeholder="資産番号 / 端末名 / 社員名"
          @keyup.enter="doSearch" />
        <select v-model="searchForm.returned" class="select">
          <option value="">すべて</option>
          <option value="false">貸出中</option>
          <option value="true">返却済</option>
        </select>
        <button class="btn btn-secondary" @click="doSearch">
          <img src="@/image/icon-search.svg" class="btn-icon-img" alt="検索" /> 検索
        </button>
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
                <th>貸出先</th>
                <th>貸出日</th>
                <th>返却期限</th>
                <th>ステータス</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loans.length === 0">
                <td colspan="7" class="empty">データがありません</td>
              </tr>
              <tr v-for="l in loans" :key="l.id" :class="{ 'row-overdue': l.overdue }">
                <td class="mono">{{ l.assetNumber }}</td>
                <td>{{ l.deviceName }}</td>
                <td>{{ l.employeeCode }} {{ l.employeeName }}</td>
                <td>{{ formatDate(l.loanDate) }}</td>
                <td :class="{ 'text-danger': l.overdue }">
                  {{ l.expectedReturnDate ? formatDate(l.expectedReturnDate) : '—' }}
                  <span v-if="l.overdue" class="overdue-tag">期限超過</span>
                </td>
                <td>
                  <span class="status-badge" :class="l.returned ? 'returned' : 'active'">
                    {{ l.returned ? '返却済' : '貸出中' }}
                  </span>
                </td>
                <td>
                  <button
                    v-if="!l.returned && authStore.isItStaff"
                    class="btn btn-sm btn-warning"
                    @click="openReturn(l)"
                  >返却登録</button>
                </td>
              </tr>
            </tbody>
          </table>
          <AppPagination v-model="currentPage" :total-pages="totalPages"
            :total-elements="totalElements" :size="pageSize" />
        </template>
      </div>
    </div>

    <!-- 貸出登録モーダル -->
    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal">
        <h3 class="modal-title">貸出登録</h3>
        <div class="form-grid">
          <div class="form-group full">
            <label>PC資産 <span class="required">*</span></label>
            <select v-model="createForm.pcAssetId" class="select">
              <option :value="null">選択してください</option>
              <option v-for="a in availableAssets" :key="a.id" :value="a.id">
                {{ a.assetNumber }} {{ a.deviceName }}
              </option>
            </select>
          </div>
          <div class="form-group full">
            <label>貸出先社員 <span class="required">*</span></label>
            <select v-model="createForm.employeeId" class="select">
              <option :value="null">選択してください</option>
              <option v-for="e in activeEmployees" :key="e.id" :value="e.id">
                {{ e.employeeCode }} {{ e.fullName }}
              </option>
            </select>
          </div>
          <div class="form-group">
            <label>貸出日 <span class="required">*</span></label>
            <input v-model="createForm.loanDate" type="date" class="input" />
          </div>
          <div class="form-group">
            <label>返却予定日</label>
            <input v-model="createForm.expectedReturnDate" type="date" class="input" />
          </div>
          <div class="form-group full">
            <label>目的</label>
            <input v-model="createForm.purpose" class="input" placeholder="例: 出張用" />
          </div>
          <div class="form-group full">
            <label>備考</label>
            <textarea v-model="createForm.note" class="textarea" rows="2" />
          </div>
        </div>
        <p v-if="formError" class="form-error">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showCreateModal = false">キャンセル</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveLoan">
            {{ saving ? '登録中…' : '登録' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 返却登録モーダル -->
    <div v-if="showReturnModal" class="modal-overlay" @click.self="showReturnModal = false">
      <div class="modal modal-sm">
        <h3 class="modal-title">返却登録</h3>
        <p class="return-target">
          <strong>{{ returnTarget?.assetNumber }}</strong> {{ returnTarget?.deviceName }}
          （{{ returnTarget?.employeeName }}）
        </p>
        <div class="form-grid">
          <div class="form-group full">
            <label>返却日 <span class="required">*</span></label>
            <input v-model="returnForm.actualReturnDate" type="date" class="input" />
          </div>
          <div class="form-group full">
            <label>備考</label>
            <textarea v-model="returnForm.note" class="textarea" rows="2" />
          </div>
        </div>
        <p v-if="formError" class="form-error">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showReturnModal = false">キャンセル</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveReturn">
            {{ saving ? '登録中…' : '返却登録' }}
          </button>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { loansApi } from '@/api/loans'
import { assetsApi } from '@/api/assets'
import { employeesApi } from '@/api/employees'
import AppLayout from '@/components/common/AppLayout.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import type { PcLoan, PcAsset, Employee } from '@/types'

const authStore = useAuthStore()

const loans = ref<PcLoan[]>([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const pageSize = 20
const totalPages = ref(0)
const totalElements = ref(0)

const searchForm = reactive({ keyword: '', returned: '' as '' | 'true' | 'false' })

function formatDate(v: string | null): string {
  if (!v) return '—'
  return new Date(v + 'T00:00:00').toLocaleDateString('ja-JP')
}

async function loadLoans() {
  loading.value = true; error.value = ''
  try {
    const res = await loansApi.findAll({
      page: currentPage.value, size: pageSize,
      keyword: searchForm.keyword || undefined,
      returned: searchForm.returned === '' ? undefined : searchForm.returned === 'true',
    })
    loans.value = res.data.content
    totalPages.value = res.data.totalPages
    totalElements.value = res.data.totalElements
  } catch (e: any) { error.value = e?.response?.data?.message ?? '取得失敗' }
  finally { loading.value = false }
}

function doSearch() { currentPage.value = 0; loadLoans() }
function resetSearch() { searchForm.keyword = ''; searchForm.returned = ''; doSearch() }
watch(currentPage, loadLoans)

// 貸出可能PC・社員リスト
const availableAssets = ref<PcAsset[]>([])
const activeEmployees = ref<Employee[]>([])

async function loadSelects() {
  const [aRes, eRes] = await Promise.all([
    assetsApi.findAll({ size: 200, statuses: 'IN_STORAGE' }),
    employeesApi.findActiveList(),
  ])
  availableAssets.value = aRes.data.content
  activeEmployees.value = eRes.data
}

// 貸出登録
const showCreateModal = ref(false)
const saving = ref(false)
const formError = ref('')
const createForm = reactive({
  pcAssetId: null as number | null,
  employeeId: null as number | null,
  loanDate: new Date().toISOString().slice(0, 10),
  expectedReturnDate: '',
  purpose: '',
  note: '',
})

function openCreate() {
  Object.assign(createForm, {
    pcAssetId: null, employeeId: null,
    loanDate: new Date().toISOString().slice(0, 10),
    expectedReturnDate: '', purpose: '', note: '',
  })
  formError.value = ''; showCreateModal.value = true
}

async function saveLoan() {
  if (!createForm.pcAssetId) { formError.value = 'PC資産を選択してください'; return }
  if (!createForm.employeeId) { formError.value = '社員を選択してください'; return }
  saving.value = true
  try {
    await loansApi.create({
      pcAssetId: createForm.pcAssetId!,
      employeeId: createForm.employeeId!,
      loanDate: createForm.loanDate,
      expectedReturnDate: createForm.expectedReturnDate || undefined,
      purpose: createForm.purpose || undefined,
      note: createForm.note || undefined,
    })
    showCreateModal.value = false; loadLoans()
  } catch (e: any) { formError.value = e?.response?.data?.message ?? '登録失敗' }
  finally { saving.value = false }
}

// 返却登録
const showReturnModal = ref(false)
const returnTarget = ref<PcLoan | null>(null)
const returnForm = reactive({ actualReturnDate: '', note: '' })

function openReturn(l: PcLoan) {
  returnTarget.value = l
  returnForm.actualReturnDate = new Date().toISOString().slice(0, 10)
  returnForm.note = ''
  formError.value = ''; showReturnModal.value = true
}

async function saveReturn() {
  if (!returnForm.actualReturnDate) { formError.value = '返却日を入力してください'; return }
  saving.value = true
  try {
    await loansApi.returnLoan(returnTarget.value!.id, {
      actualReturnDate: returnForm.actualReturnDate,
      note: returnForm.note || undefined,
    })
    showReturnModal.value = false; loadLoans()
  } catch (e: any) { formError.value = e?.response?.data?.message ?? '返却登録失敗' }
  finally { saving.value = false }
}

onMounted(() => { loadLoans(); loadSelects() })
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title  { font-size: 20px; font-weight: 700; color: #1a1a2e; margin: 0; }
.search-bar  { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; padding: 14px 16px; }
.card { background: white; border-radius: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.07); }
.table-card { padding: 16px; }

.input, .select {
  height: 36px; border: 1px solid #d1d5db; border-radius: 6px;
  padding: 0 10px; font-size: 13px; outline: none;
}
.input { min-width: 220px; } .input:focus, .select:focus { border-color: #6366f1; }
.btn { height: 36px; padding: 0 16px; border-radius: 6px; font-size: 13px; cursor: pointer; border: none; font-weight: 500; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary   { background: #6366f1; color: white; }
.btn-primary:hover { background: #4f46e5; }
.btn-secondary { background: #f3f4f6; color: #374151; border: 1px solid #d1d5db; }
.btn-ghost     { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }
.btn-warning   { background: #f59e0b; color: white; height: 30px; padding: 0 12px; font-size: 12px; }
.btn-sm        { height: 28px; padding: 0 10px; font-size: 12px; }

.table { width: 100%; border-collapse: collapse; font-size: 13px; }
.table th { background: #f9fafb; padding: 10px 12px; text-align: left; font-weight: 600; color: #374151; border-bottom: 1px solid #e5e7eb; }
.table td { padding: 10px 12px; border-bottom: 1px solid #f3f4f6; }
.empty { text-align: center; color: #9ca3af; padding: 40px; }
.mono { font-family: monospace; }
.row-overdue td { background: #fff5f5; }
.text-danger { color: #ef4444; font-weight: 600; }
.overdue-tag { margin-left: 6px; background: #fef2f2; color: #dc2626; font-size: 10px; padding: 1px 6px; border-radius: 4px; }

.status-badge { display: inline-block; padding: 2px 10px; border-radius: 999px; font-size: 12px; font-weight: 600; }
.active   { background: #fef3c7; color: #92400e; }
.returned { background: #f3f4f6; color: #6b7280; }

.loading { padding: 40px; text-align: center; color: #9ca3af; }
.error-msg { padding: 16px; color: #ef4444; }

.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: white; border-radius: 12px; padding: 28px; width: 560px; max-width: 95vw; max-height: 90vh; overflow-y: auto; }
.modal-sm { width: 420px; }
.modal-title { font-size: 18px; font-weight: 700; margin: 0 0 16px; color: #1a1a2e; }
.return-target { margin: 0 0 16px; font-size: 14px; color: #374151; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group.full { grid-column: 1 / -1; }
.form-group label { font-size: 12px; font-weight: 600; color: #374151; }
.textarea { border: 1px solid #d1d5db; border-radius: 6px; padding: 8px 10px; font-size: 13px; resize: vertical; outline: none; }
.required { color: #ef4444; }
.form-error { color: #ef4444; font-size: 13px; margin-top: 8px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
</style>
