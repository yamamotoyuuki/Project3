<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <h2 class="page-title">
          <img src="@/image/icon-rental.svg" class="page-title-icon" alt="レンタル管理" />
          レンタル管理
        </h2>
        <div class="header-actions">
          <button v-if="authStore.isItStaff" class="btn btn-secondary" @click="showVendorModal = true">
            ベンダー管理
          </button>
          <button v-if="authStore.isItStaff" class="btn btn-primary" @click="openCreate">
            ＋ 契約登録
          </button>
        </div>
      </div>

      <!-- フィルタバー -->
      <div class="search-bar card">
        <input v-model="searchForm.keyword" class="input" placeholder="資産番号 / 端末名 / ベンダー / 契約番号"
          @keyup.enter="doSearch" />
        <select v-model="searchForm.expiryFilter" class="select">
          <option value="">期限 : すべて</option>
          <option value="near">90日以内に期限切れ</option>
          <option value="expired">期限切れ済み</option>
        </select>
        <select v-model="searchForm.returned" class="select">
          <option value="">返却 : すべて</option>
          <option value="false">契約中</option>
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
                <th>ベンダー</th>
                <th>契約番号</th>
                <th>開始日</th>
                <th>終了日</th>
                <th>月額</th>
                <th>残日数</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="rentals.length === 0">
                <td colspan="9" class="empty">データがありません</td>
              </tr>
              <tr v-for="r in rentals" :key="r.id" :class="{ 'row-expired': r.expired, 'row-near': !r.expired && r.daysUntilExpiry !== null && r.daysUntilExpiry <= 90 }">
                <td class="mono">{{ r.assetNumber }}</td>
                <td>{{ r.deviceName }}</td>
                <td>{{ r.vendorName }}</td>
                <td class="small-text">{{ r.contractNumber || '—' }}</td>
                <td>{{ formatDate(r.rentalStartDate) }}</td>
                <td :class="{ 'text-danger': r.expired }">{{ formatDate(r.rentalEndDate) }}</td>
                <td class="small-text">{{ r.monthlyFee ? `¥${r.monthlyFee.toLocaleString()}` : '—' }}</td>
                <td>
                  <span v-if="r.returned" class="small-text text-muted">返却済</span>
                  <span v-else-if="r.expired" class="text-danger font-bold">期限切れ</span>
                  <span v-else-if="r.daysUntilExpiry !== null">
                    <span :class="r.daysUntilExpiry <= 30 ? 'text-danger' : r.daysUntilExpiry <= 90 ? 'text-warning' : ''">
                      {{ r.daysUntilExpiry }}日
                    </span>
                  </span>
                  <span v-else>—</span>
                </td>
                <td>
                  <button
                    v-if="!r.returned && authStore.isItStaff"
                    class="btn btn-sm btn-secondary"
                    @click="doReturn(r)"
                  >返却</button>
                </td>
              </tr>
            </tbody>
          </table>
          <AppPagination v-model="currentPage" :total-pages="totalPages"
            :total-elements="totalElements" :size="pageSize" />
        </template>
      </div>
    </div>

    <!-- 契約登録モーダル -->
    <div v-if="showCreateModal" class="modal-overlay" @click.self="showCreateModal = false">
      <div class="modal">
        <h3 class="modal-title">レンタル契約登録</h3>
        <div class="form-grid">
          <div class="form-group full">
            <label>PC資産 <span class="required">*</span></label>
            <select v-model="createForm.pcAssetId" class="select">
              <option :value="null">選択してください</option>
              <option v-for="a in rentalAssets" :key="a.id" :value="a.id">
                {{ a.assetNumber }} {{ a.deviceName }}
              </option>
            </select>
          </div>
          <div class="form-group full">
            <label>ベンダー <span class="required">*</span></label>
            <select v-model="createForm.rentalVendorId" class="select">
              <option :value="null">選択してください</option>
              <option v-for="v in vendors" :key="v.id" :value="v.id">{{ v.companyName }}</option>
            </select>
          </div>
          <div class="form-group full">
            <label>契約番号</label>
            <input v-model="createForm.contractNumber" class="input" />
          </div>
          <div class="form-group">
            <label>開始日 <span class="required">*</span></label>
            <input v-model="createForm.rentalStartDate" type="date" class="input" />
          </div>
          <div class="form-group">
            <label>終了日 <span class="required">*</span></label>
            <input v-model="createForm.rentalEndDate" type="date" class="input" />
          </div>
          <div class="form-group full">
            <label>月額（円）</label>
            <input v-model.number="createForm.monthlyFee" type="number" class="input" />
          </div>
        </div>
        <p v-if="formError" class="form-error">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showCreateModal = false">キャンセル</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveRental">
            {{ saving ? '登録中…' : '登録' }}
          </button>
        </div>
      </div>
    </div>

    <!-- ベンダー管理モーダル -->
    <div v-if="showVendorModal" class="modal-overlay" @click.self="showVendorModal = false">
      <div class="modal">
        <h3 class="modal-title">ベンダー管理</h3>
        <table class="table" style="margin-bottom:16px">
          <thead><tr><th>会社名</th><th>担当者</th><th>電話</th><th>操作</th></tr></thead>
          <tbody>
            <tr v-for="v in vendors" :key="v.id">
              <td>{{ v.companyName }}</td>
              <td>{{ v.contactName || '—' }}</td>
              <td>{{ v.phone || '—' }}</td>
              <td><button class="btn-icon" @click="editVendor(v)"><img src="@/image/icon-edit.svg" alt="編集" /></button></td>
            </tr>
          </tbody>
        </table>
        <h4 class="subsection">{{ editingVendor ? 'ベンダー編集' : 'ベンダー追加' }}</h4>
        <div class="form-grid">
          <div class="form-group full">
            <label>会社名 <span class="required">*</span></label>
            <input v-model="vendorForm.companyName" class="input" />
          </div>
          <div class="form-group">
            <label>担当者名</label>
            <input v-model="vendorForm.contactName" class="input" />
          </div>
          <div class="form-group">
            <label>電話番号</label>
            <input v-model="vendorForm.phone" class="input" />
          </div>
          <div class="form-group full">
            <label>メールアドレス</label>
            <input v-model="vendorForm.email" class="input" type="email" />
          </div>
        </div>
        <p v-if="vendorError" class="form-error">{{ vendorError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="showVendorModal = false">閉じる</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveVendor">
            {{ saving ? '保存中…' : editingVendor ? '更新' : '追加' }}
          </button>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { rentalsApi } from '@/api/rentals'
import { assetsApi } from '@/api/assets'
import AppLayout from '@/components/AppLayout.vue'
import AppPagination from '@/components/AppPagination.vue'
import type { PcRental, RentalVendor, PcAsset } from '@/types'

const authStore = useAuthStore()
const rentals = ref<PcRental[]>([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const pageSize = 20
const totalPages = ref(0)
const totalElements = ref(0)

const searchForm = reactive({
  keyword: '',
  expiryFilter: '' as '' | 'near' | 'expired',
  returned: '' as '' | 'true' | 'false',
})

function formatDate(v: string | null): string {
  if (!v) return '—'
  return new Date(v + 'T00:00:00').toLocaleDateString('ja-JP')
}

async function loadRentals() {
  loading.value = true; error.value = ''
  try {
    const res = await rentalsApi.findAll({
      page: currentPage.value, size: pageSize,
      keyword: searchForm.keyword || undefined,
      expiryFilter: searchForm.expiryFilter || undefined,
      returned: searchForm.returned === '' ? undefined : searchForm.returned === 'true',
    })
    rentals.value = res.data.content
    totalPages.value = res.data.totalPages
    totalElements.value = res.data.totalElements
  } catch (e: any) { error.value = e?.response?.data?.message ?? '取得失敗' }
  finally { loading.value = false }
}

function doSearch() { currentPage.value = 0; loadRentals() }
function resetSearch() { searchForm.keyword = ''; searchForm.expiryFilter = ''; searchForm.returned = ''; doSearch() }
watch(currentPage, loadRentals)

// 返却
const saving = ref(false)
async function doReturn(r: PcRental) {
  if (!confirm(`「${r.deviceName}」の返却を登録しますか？`)) return
  try {
    await rentalsApi.returnRental(r.id)
    loadRentals()
  } catch (e: any) { alert(e?.response?.data?.message ?? '返却登録失敗') }
}

// ベンダー
const vendors = ref<RentalVendor[]>([])
const showVendorModal = ref(false)
const editingVendor = ref<RentalVendor | null>(null)
const vendorError = ref('')
const vendorForm = reactive({ companyName: '', contactName: '', phone: '', email: '', address: '', note: '' })

async function loadVendors() {
  try { vendors.value = (await rentalsApi.findAllVendors()).data }
  catch { /* ignore */ }
}

function editVendor(v: RentalVendor) {
  editingVendor.value = v
  Object.assign(vendorForm, { companyName: v.companyName, contactName: v.contactName ?? '', phone: v.phone ?? '', email: v.email ?? '', address: v.address ?? '', note: v.note ?? '' })
}

async function saveVendor() {
  if (!vendorForm.companyName) { vendorError.value = '会社名は必須です'; return }
  saving.value = true
  try {
    if (editingVendor.value) {
      await rentalsApi.updateVendor(editingVendor.value.id, vendorForm)
    } else {
      await rentalsApi.createVendor(vendorForm)
    }
    editingVendor.value = null
    Object.assign(vendorForm, { companyName: '', contactName: '', phone: '', email: '', address: '', note: '' })
    vendorError.value = ''
    loadVendors()
  } catch (e: any) { vendorError.value = e?.response?.data?.message ?? '保存失敗' }
  finally { saving.value = false }
}

// 契約登録
const showCreateModal = ref(false)
const formError = ref('')
const rentalAssets = ref<PcAsset[]>([])
const createForm = reactive({
  pcAssetId: null as number | null,
  rentalVendorId: null as number | null,
  contractNumber: '',
  rentalStartDate: '',
  rentalEndDate: '',
  monthlyFee: null as number | null,
})

async function openCreate() {
  Object.assign(createForm, { pcAssetId: null, rentalVendorId: null, contractNumber: '', rentalStartDate: '', rentalEndDate: '', monthlyFee: null })
  formError.value = ''
  const res = await assetsApi.findAll({ size: 200, acquisitionType: 'RENTAL' })
  rentalAssets.value = res.data.content
  showCreateModal.value = true
}

async function saveRental() {
  if (!createForm.pcAssetId) { formError.value = 'PC資産を選択してください'; return }
  if (!createForm.rentalVendorId) { formError.value = 'ベンダーを選択してください'; return }
  if (!createForm.rentalStartDate || !createForm.rentalEndDate) { formError.value = '開始日と終了日を入力してください'; return }
  saving.value = true
  try {
    await rentalsApi.create({
      pcAssetId: createForm.pcAssetId!,
      rentalVendorId: createForm.rentalVendorId!,
      contractNumber: createForm.contractNumber || undefined,
      rentalStartDate: createForm.rentalStartDate,
      rentalEndDate: createForm.rentalEndDate,
      monthlyFee: createForm.monthlyFee ?? undefined,
    })
    showCreateModal.value = false; loadRentals()
  } catch (e: any) { formError.value = e?.response?.data?.message ?? '登録失敗' }
  finally { saving.value = false }
}

onMounted(() => { loadRentals(); loadVendors() })
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.header-actions { display: flex; gap: 10px; }
.page-title { font-size: 20px; font-weight: 700; color: #1a1a2e; margin: 0; }
.search-bar { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; padding: 14px 16px; }
.card { background: white; border-radius: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.07); }
.table-card { padding: 16px; }
.input, .select { height: 36px; border: 1px solid #d1d5db; border-radius: 6px; padding: 0 10px; font-size: 13px; outline: none; }
.input { min-width: 200px; } .input:focus, .select:focus { border-color: #6366f1; }
.btn { height: 36px; padding: 0 16px; border-radius: 6px; font-size: 13px; cursor: pointer; border: none; font-weight: 500; }
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary   { background: #6366f1; color: white; }
.btn-primary:hover { background: #4f46e5; }
.btn-secondary { background: #f3f4f6; color: #374151; border: 1px solid #d1d5db; }
.btn-ghost     { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }
.btn-sm { height: 28px; padding: 0 10px; font-size: 12px; }
.table { width: 100%; border-collapse: collapse; font-size: 13px; }
.table th { background: #f9fafb; padding: 10px 12px; text-align: left; font-weight: 600; color: #374151; border-bottom: 1px solid #e5e7eb; }
.table td { padding: 10px 12px; border-bottom: 1px solid #f3f4f6; }
.empty { text-align: center; color: #9ca3af; padding: 40px; }
.mono { font-family: monospace; }
.small-text { font-size: 12px; color: #6b7280; }
.text-muted  { color: #9ca3af; }
.text-danger { color: #ef4444; }
.text-warning { color: #f59e0b; }
.font-bold { font-weight: 700; }
.row-expired td { background: #fff5f5; }
.row-near td { background: #fffbeb; }
.loading { padding: 40px; text-align: center; color: #9ca3af; }
.error-msg { padding: 16px; color: #ef4444; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 15px; padding: 4px; border-radius: 4px; }
.btn-icon:hover { background: #f3f4f6; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: white; border-radius: 12px; padding: 28px; width: 600px; max-width: 95vw; max-height: 90vh; overflow-y: auto; }
.modal-title { font-size: 18px; font-weight: 700; margin: 0 0 20px; color: #1a1a2e; }
.subsection { font-size: 14px; font-weight: 700; color: #374151; margin: 16px 0 10px; border-top: 1px solid #e5e7eb; padding-top: 14px; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group.full { grid-column: 1 / -1; }
.form-group label { font-size: 12px; font-weight: 600; color: #374151; }
.required { color: #ef4444; }
.form-error { color: #ef4444; font-size: 13px; margin-top: 8px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
</style>
