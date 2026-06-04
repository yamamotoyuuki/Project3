<!--
  機能ID: WEB_AST0101
  views/WEB_AST0101_AssetListView.vue
  -----------------------------------------------
  資産一覧画面コンポーネント
  -----------------------------------------------
-->
<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <!-- ページタイトル（imageフォルダのアイコン画像を使用。規約準拠） -->
        <h2 class="page-title">
          <img src="@/image/icon-pc.svg" class="page-title-icon" alt="PC" />
          機器一覧
        </h2>
        <div class="header-actions">
          <!-- CSV / Excel エクスポートボタン（imageフォルダのアイコン画像を使用） -->
          <a class="btn btn-ghost" :href="exportUrl('assets.csv')" download>
            <img src="@/image/icon-csv.svg" class="btn-icon-img" alt="CSV" /> CSV
          </a>
          <a class="btn btn-ghost" :href="exportUrl('assets.xlsx')" download>
            <img src="@/image/icon-excel.svg" class="btn-icon-img" alt="Excel" /> Excel
          </a>
          <button v-if="authStore.isItStaff" class="btn btn-primary" @click="openCreate">
            ＋ 機器登録
          </button>
        </div>
      </div>

      <!-- 検索バー -->
      <div class="search-bar card">
        <!-- キーワード検索（資産番号） -->
        <input
          v-model="searchForm.keyword"
          class="input"
          placeholder="資産番号"
          @keyup.enter="doSearch"
        />
        <!-- 機器種別フィルタ（複数選択・含む/除く対応。共通コンポーネント使用） -->
        <MultiSelectFilter
          label="機器種別"
          :options="deviceTypeOptions"
          v-model="searchForm.deviceTypeFilter"
        />
        <!-- 取得区分フィルタ（複数選択・含む/除く対応。共通コンポーネント使用） -->
        <MultiSelectFilter
          label="取得区分"
          :options="acquisitionTypeOptions"
          v-model="searchForm.acquisitionTypeFilter"
        />
        <!-- ステータスフィルタ（複数選択・含む/除く対応。共通コンポーネント使用） -->
        <MultiSelectFilter
          label="ステータス"
          :options="statusOptions"
          v-model="searchForm.statusFilter"
        />
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
                <th>機器種別</th>
                <th>取得区分</th>
                <th>ステータス</th>
                <th>使用者</th>
                <th>場所</th>
                <th>エージェント番号</th>
                <th>詳細</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="assets.length === 0">
                <td colspan="9" class="empty">データがありません</td>
              </tr>
              <tr v-for="a in assets" :key="a.id">
                <td class="mono">{{ a.assetNumber }}</td>
                <!-- 機器種別: deviceTypeOptions から codeLabel を引いて表示する -->
                <td>{{ deviceTypeLabel(a.deviceType) }}</td>
                <td>
                  <!--
                    レンタル: ボタンとして表示し、クリックでレンタル一覧へ遷移する。
                    遷移先では資産番号をキーワードとして渡し、該当契約を絞り込む。
                    購入: 遷移先がないためスパン（バッジ表示のみ）とする。
                  -->
                  <button
                    v-if="a.acquisitionType === 'RENTAL'"
                    class="acq-badge rental acq-link"
                    @click="goToRentalList(a.assetNumber)"
                  >レンタル</button>
                  <span v-else class="acq-badge purchase">購入</span>
                </td>
                <td><StatusBadge :status="a.status" /></td>
                <!-- 社員マスタ連携あり: assignedEmployeeName、エージェント入力のみ: userName -->
                <td>{{ a.assignedEmployeeName || a.userName || '—' }}</td>
                <td class="small-text">{{ a.location || '—' }}</td>
                <!-- エージェント番号（未導入の場合は「—」を表示） -->
                <td class="mono small-text">{{ a.agentNumber || '—' }}</td>
                <td>
                  <!-- 編集ボタン押下で詳細画面へ遷移（編集はすべて詳細画面で行う） -->
                  <button
                    v-if="authStore.isItStaff"
                    class="btn-icon"
                    title="編集"
                    @click="goToAssetDetail(a.id)"
                  ><img src="@/image/icon-edit.svg" alt="編集" /></button>
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

    <!-- 登録モーダル（新規登録専用。編集は詳細画面で行う） -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h3 class="modal-title">機器登録</h3>

        <div class="form-grid">
          <div class="form-group">
            <label>資産番号 <span class="required">*</span></label>
            <input v-model="form.assetNumber" class="input" placeholder="例: PC-2024-001" />
          </div>
          <div class="form-group">
            <label>機器種別</label>
            <!--
              コードマスタ API（DEVICE_TYPE）から取得した選択肢を表示する。
              codeValue を v-model の値として、codeLabel をラベルとして使用する。
              未選択（""）を先頭に配置し、任意入力とする。
            -->
            <select v-model="form.deviceType" class="select">
              <option value="">（未選択）</option>
              <option
                v-for="opt in deviceTypeOptions"
                :key="opt.codeValue"
                :value="opt.codeValue"
              >{{ opt.codeLabel }}</option>
            </select>
          </div>
          <div class="form-group full">
            <!-- 端末名は任意項目のため、必須マーク（*）なし -->
            <label>端末名</label>
            <input v-model="form.deviceName" class="input" placeholder="例: ThinkPad X1 Carbon" />
          </div>
          <div class="form-group">
            <label>取得区分 <span class="required">*</span></label>
            <!--
              コードマスタ API（ACQUISITION_TYPE）から取得した選択肢を表示する。
              codeValue を v-model の値として、codeLabel をラベルとして使用する。
              API 取得前（acquisitionTypeOptions が空）は選択肢なし状態で表示される。
            -->
            <select v-model="form.acquisitionType" class="select">
              <option
                v-for="opt in acquisitionTypeOptions"
                :key="opt.codeValue"
                :value="opt.codeValue"
              >{{ opt.codeLabel }}</option>
            </select>
          </div>
          <div class="form-group">
            <label>ステータス <span class="required">*</span></label>
            <!--
              コードマスタ API（PC_STATUS）から取得した選択肢を表示する。
              codeValue を v-model の値として、codeLabel をラベルとして使用する。
              API 取得前（statusOptions が空）は選択肢なし状態で表示される。
            -->
            <select v-model="form.status" class="select">
              <option
                v-for="opt in statusOptions"
                :key="opt.codeValue"
                :value="opt.codeValue"
              >{{ opt.codeLabel }}</option>
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
import { commonApi } from '@/api/common'
import AppLayout from '@/components/common/AppLayout.vue'
import StatusBadge from '@/components/common/StatusBadge.vue'
import AppPagination from '@/components/common/AppPagination.vue'
import MultiSelectFilter from '@/components/common/MultiSelectFilter.vue'
import type { Employee, PcAsset, CodeValue, MultiFilterValue, AcquisitionType, PcStatus } from '@/types'

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
  keyword:              '',
  // 各フィルタの初期状態（values 空 = フィルタなし = 全件表示）
  statusFilter:          { values: [] as string[] } as MultiFilterValue,
  acquisitionTypeFilter: { values: [] as string[] } as MultiFilterValue,
  deviceTypeFilter:      { values: [] as string[] } as MultiFilterValue,
})

/**
 * MultiFilterValue を API クエリパラメータ文字列に変換する。
 * 未選択（values 空）または全選択の場合はフィルタなし（undefined）を返す。
 * @param filter     - フィルタ選択状態
 * @param allOptions - 全選択肢（全選択判定に使用）
 */
function buildFilterParam(
  filter:     MultiFilterValue,
  allOptions: CodeValue[],
): string | undefined {
  const { values } = filter
  // 未選択 or 全選択 → フィルタなし（undefined = パラメータを送信しない）
  if (values.length === 0 || values.length === allOptions.length) return undefined
  return values.join(',')
}

async function loadAssets() {
  loading.value = true
  error.value = ''
  try {
    const res = await assetsApi.findAll({
      page:             currentPage.value,
      size:             pageSize,
      keyword:          searchForm.keyword || undefined,
      // チェック済み項目のカンマ区切り文字列を渡す（全件・未選択時は undefined）
      statuses:         buildFilterParam(searchForm.statusFilter,          statusOptions.value),
      acquisitionTypes: buildFilterParam(searchForm.acquisitionTypeFilter, acquisitionTypeOptions.value),
      deviceTypes:      buildFilterParam(searchForm.deviceTypeFilter,      deviceTypeOptions.value),
    })
    assets.value = res.data.content
    totalPages.value = res.data.totalPages
    totalElements.value = res.data.totalElements
  } catch (e: unknown) {
    error.value = (e as { response?: { data?: { message?: string } } })
      ?.response?.data?.message ?? 'データの取得に失敗しました'
  } finally {
    loading.value = false
  }
}

function doSearch() {
  currentPage.value = 0
  loadAssets()
}

/**
 * 資産詳細画面へ遷移する。
 * @param id - 遷移先の資産 ID
 */
function goToAssetDetail(id: number): void {
  router.push(`/assets/${id}`)
}

/**
 * レンタル一覧画面へ遷移する。
 * 資産番号をキーワードとして渡し、該当契約を絞り込んだ状態で表示する。
 * @param assetNumber - 絞り込みに使用する資産番号
 */
function goToRentalList(assetNumber: string): void {
  router.push({ path: '/rentals', query: { keyword: assetNumber } })
}

function resetSearch() {
  searchForm.keyword             = ''
  // 各フィルタを初期状態（全件表示）にリセットする
  searchForm.statusFilter          = { values: [] }
  searchForm.acquisitionTypeFilter = { values: [] }
  searchForm.deviceTypeFilter      = { values: [] }
  currentPage.value = 0
  loadAssets()
}

watch(currentPage, loadAssets)

// ---- 取得区分プルダウン（コードマスタ API から取得） ----
/** 取得区分の選択肢（ACQUISITION_TYPE）。初期表示時に API から取得してセットする。 */
const acquisitionTypeOptions = ref<CodeValue[]>([])

/**
 * 取得区分のコード値一覧を共通コードマスタ API から取得する。
 * is_active=1 かつ sort_order ASC の順で返却される。
 * API エラー時はコンソールにのみ出力し、モーダルには空ドロップダウンとして表示する。
 */
async function loadAcquisitionTypes(): Promise<void> {
  try {
    const res = await commonApi.getCodeValues('ACQUISITION_TYPE')
    acquisitionTypeOptions.value = res.data
    // 取得した選択肢の先頭をフォームの初期値に設定する
    if (acquisitionTypeOptions.value.length > 0) {
      form.acquisitionType = acquisitionTypeOptions.value[0].codeValue as AcquisitionType
    }
  } catch (e) {
    console.error('取得区分の取得に失敗しました', e)
  }
}

// ---- ステータスプルダウン（コードマスタ API から取得） ----
/** ステータスの選択肢（PC_STATUS）。初期表示時に API から取得してセットする。 */
const statusOptions = ref<CodeValue[]>([])

/**
 * ステータスのコード値一覧を共通コードマスタ API から取得する。
 * is_active=1 かつ sort_order ASC の順で返却される。
 * API エラー時はコンソールにのみ出力し、モーダルには空ドロップダウンとして表示する。
 */
async function loadStatusOptions(): Promise<void> {
  try {
    const res = await commonApi.getCodeValues('PC_STATUS')
    statusOptions.value = res.data
    // 取得した選択肢の先頭をフォームの初期値に設定する
    if (statusOptions.value.length > 0) {
      form.status = statusOptions.value[0].codeValue as PcStatus
    }
  } catch (e) {
    console.error('ステータスの取得に失敗しました', e)
  }
}

// ---- 機器種別プルダウン（コードマスタ API から取得） ----
/** 機器種別の選択肢（DEVICE_TYPE）。初期表示時に API から取得してセットする。 */
const deviceTypeOptions = ref<CodeValue[]>([])

/**
 * 機器種別のコード値一覧を共通コードマスタ API から取得する。
 * is_active=1 かつ sort_order ASC の順で返却される。
 * 任意項目のため、API エラー時はコンソールにのみ出力する。
 */
async function loadDeviceTypes(): Promise<void> {
  try {
    const res = await commonApi.getCodeValues('DEVICE_TYPE')
    deviceTypeOptions.value = res.data
  } catch (e) {
    console.error('機器種別の取得に失敗しました', e)
  }
}

/**
 * 機器種別コード値を表示ラベルに変換する。
 * deviceTypeOptions から codeValue に一致する codeLabel を返す。
 * 未設定（null）または一致なしの場合は「—」を返す。
 * @param code - DEVICE_TYPE のコード値（例: "LAPTOP", "DESKTOP"）
 */
function deviceTypeLabel(code: string | null): string {
  if (!code) return '—'
  return deviceTypeOptions.value.find((o) => o.codeValue === code)?.codeLabel ?? '—'
}

// ---- 社員プルダウン ----
const activeEmployees = ref<Employee[]>([])

async function loadActiveEmployees() {
  try {
    const res = await employeesApi.findActiveList()
    activeEmployees.value = res.data
  } catch { /* ignore */ }
}

// ---- 登録モーダル（新規登録専用。編集は詳細画面で行う） ----
const showModal = ref(false)
const saving = ref(false)
const formError = ref('')

const form = reactive({
  assetNumber: '',
  deviceName: '',
  deviceType: '',        // 機器種別（DEVICE_TYPE コード値。空文字 = 未選択）
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
  // 各プルダウンの初期値は API 取得済み選択肢の先頭を使用する。
  // API 取得前（空配列）の場合はフォールバック値（PURCHASE / IN_STORAGE）を使用する。
  const defaultAcquisitionType =
    (acquisitionTypeOptions.value[0]?.codeValue ?? 'PURCHASE') as AcquisitionType
  const defaultStatus =
    (statusOptions.value[0]?.codeValue ?? 'IN_STORAGE') as PcStatus

  Object.assign(form, {
    assetNumber: '', deviceName: '',
    deviceType: '',   // 機器種別は未選択（任意項目）でリセットする
    acquisitionType: defaultAcquisitionType,
    status: defaultStatus,
    maker: '', modelNumber: '', serialNumber: '',
    hostname: '', location: '', assignedEmployeeId: null, note: '',
  })
  formError.value = ''
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

async function saveAsset() {
  formError.value = ''
  // 資産番号は必須。端末名は任意項目のためバリデーション対象外。
  if (!form.assetNumber) { formError.value = '資産番号は必須です'; return }
  saving.value = true
  try {
    await assetsApi.create({
      assetNumber:        form.assetNumber,
      deviceName:         form.deviceName,
      deviceType:         form.deviceType || undefined,  // 未選択（空文字）は送信しない
      acquisitionType:    form.acquisitionType,
      status:             form.status,
      maker:              form.maker || undefined,
      modelNumber:        form.modelNumber || undefined,
      serialNumber:       form.serialNumber || undefined,
      hostname:           form.hostname || undefined,
      location:           form.location || undefined,
      assignedEmployeeId: form.assignedEmployeeId,
      note:               form.note || undefined,
    })
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
  // 初期表示時にコードマスタ API からプルダウン選択肢を取得する
  loadAcquisitionTypes()
  loadStatusOptions()
  loadDeviceTypes()
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
/* レンタルバッジをボタンとして表示するためのリセット・クリック可能スタイル */
.acq-link {
  border: none;
  cursor: pointer;
  letter-spacing: 0.01em;
}
.acq-link:hover { opacity: 0.75; }

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
