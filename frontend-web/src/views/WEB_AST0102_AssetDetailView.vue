<!--
  機能ID: WEB_AST0102
  views/WEB_AST0102_AssetDetailView.vue
  -----------------------------------------------
  資産詳細画面コンポーネント
  -----------------------------------------------
-->
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
            <!-- imageフォルダのアイコン画像を読み込む（規約準拠） -->
            <button v-if="authStore.isItStaff" class="btn btn-primary" @click="openEdit">
              <img src="@/image/icon-edit.svg" class="btn-icon-img" alt="編集" /> 編集
            </button>
            <button
              v-if="authStore.isAdmin"
              class="btn btn-danger"
              @click="confirmDelete"
            >
              <img src="@/image/icon-delete.svg" class="btn-icon-img" alt="削除" /> 削除
            </button>
          </div>
        </div>

        <!-- タブバー -->
        <div class="tab-bar">
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'info' }"
            @click="activeTab = 'info'"
          >基本情報</button>
          <button
            class="tab-btn"
            :class="{ active: activeTab === 'software' }"
            @click="activeTab = 'software'"
          >ソフトウェア一覧</button>
        </div>

        <!-- 基本情報タブ -->
        <template v-if="activeTab === 'info'">
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
              <dt>設置場所</dt>
              <!-- エージェントが報告した設置場所を表示（読み取り専用） -->
              <dd>{{ asset.location || '—' }}</dd>
              <dt>使用者</dt>
              <!-- 社員マスタと連携できた場合は assignedEmployeeName、
                   エージェント入力のみの場合は userName を表示 -->
              <dd>{{ asset.assignedEmployeeName || asset.userName || '—' }}</dd>
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

        <!-- ソフトウェア一覧タブ -->
        <div v-else-if="activeTab === 'software'" class="card">
          <h3 class="section-title">
            ソフトウェア一覧
            <span v-if="!swLoading" class="sw-count">{{ softwareList.length }} 件</span>
          </h3>
          <!-- 読み込み中 -->
          <div v-if="swLoading" class="loading">読み込み中…</div>
          <!-- エージェント未導入 -->
          <p v-else-if="!asset.agentNumber" class="placeholder-text">
            エージェント未導入のためソフトウェア情報がありません
          </p>
          <!-- データなし -->
          <p v-else-if="softwareList.length === 0" class="placeholder-text">
            ソフトウェア情報がありません
          </p>
          <!-- ソフトウェア一覧テーブル -->
          <table v-else class="sw-table">
            <thead>
              <tr>
                <th>ソフトウェア名</th>
                <th>バージョン</th>
                <th>発行元</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="sw in softwareList" :key="sw.id">
                <td>{{ sw.softwareName }}</td>
                <td class="small-text">{{ sw.version || '—' }}</td>
                <td class="small-text">{{ sw.publisher || '—' }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </template>
    </div>

    <!-- 編集モーダル -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h3 class="modal-title">PC編集</h3>
        <div class="form-grid">
          <div class="form-group">
            <label>端末名 <span class="required">*</span></label>
            <input v-model="form.deviceName" class="input" />
          </div>
          <div class="form-group">
            <label>機器種別</label>
            <!--
              コードマスタ API（DEVICE_TYPE）から取得した選択肢を表示する。
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
          <div class="form-group">
            <label>取得区分</label>
            <!--
              コードマスタ API（ACQUISITION_TYPE）から取得した選択肢を表示する。
              openEdit() で登録済みの値を初期選択としてセットする。
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
            <label>ステータス</label>
            <!--
              コードマスタ API（PC_STATUS）から取得した選択肢を表示する。
              openEdit() で登録済みの値を初期選択としてセットする。
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
          <!-- 設置場所はエージェントが自動更新するため編集不可 -->
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
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { assetsApi } from '@/api/assets'
import { employeesApi } from '@/api/employees'
import { commonApi } from '@/api/common'
import AppLayout from '@/components/AppLayout.vue'
import StatusBadge from '@/components/StatusBadge.vue'
import type { PcAsset, Employee, AcquisitionType, PcStatus, InstalledSoftware, CodeValue } from '@/types'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const asset = ref<PcAsset | null>(null)
const loading = ref(false)
const error = ref('')

/** 詳細タブの識別型 */
type DetailTab = 'info' | 'software'

/** 現在表示中のタブ（基本情報 / ソフトウェア一覧） */
const activeTab = ref<DetailTab>('info')

// ---- ソフトウェア一覧 ----
const softwareList = ref<InstalledSoftware[]>([])
const swLoading = ref(false)

/**
 * ソフトウェア一覧をバックエンドから取得する
 * ソフトウェアタブへ切り替わったタイミングで呼び出す（初回のみ）
 */
async function loadSoftware() {
  if (swLoading.value || softwareList.value.length > 0) return
  swLoading.value = true
  try {
    const res = await assetsApi.getSoftware(Number(route.params.id))
    softwareList.value = res.data
  } catch { /* エラー時は空のまま */ }
  finally { swLoading.value = false }
}

/** ソフトウェアタブに切り替わったらデータをロードする */
watch(activeTab, (tab) => {
  if (tab === 'software') loadSoftware()
})

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

// ---- コードマスタ プルダウン（取得区分・ステータス・機器種別） ----
/** 取得区分の選択肢（ACQUISITION_TYPE） */
const acquisitionTypeOptions = ref<CodeValue[]>([])
/** ステータスの選択肢（PC_STATUS） */
const statusOptions = ref<CodeValue[]>([])
/** 機器種別の選択肢（DEVICE_TYPE） */
const deviceTypeOptions = ref<CodeValue[]>([])

/**
 * 取得区分のコード値一覧を共通コードマスタ API から取得する。
 * is_active=1 かつ sort_order ASC の順で返却される。
 */
async function loadAcquisitionTypes(): Promise<void> {
  try {
    const res = await commonApi.getCodeValues('ACQUISITION_TYPE')
    acquisitionTypeOptions.value = res.data
  } catch (e) {
    console.error('取得区分の取得に失敗しました', e)
  }
}

/**
 * ステータスのコード値一覧を共通コードマスタ API から取得する。
 * is_active=1 かつ sort_order ASC の順で返却される。
 */
async function loadStatusOptions(): Promise<void> {
  try {
    const res = await commonApi.getCodeValues('PC_STATUS')
    statusOptions.value = res.data
  } catch (e) {
    console.error('ステータスの取得に失敗しました', e)
  }
}

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
  deviceType: '',        // 機器種別（DEVICE_TYPE コード値。空文字 = 未選択）
  acquisitionType: 'PURCHASE' as AcquisitionType,
  status: 'IN_STORAGE' as PcStatus,
  maker: '',
  modelNumber: '',
  serialNumber: '',
  hostname: '',
  // location はエージェントが自動更新するため編集フォームには含めない
  assignedEmployeeId: null as number | null,
  note: '',
})

/**
 * 編集モーダルを開く。
 * コードマスタ API の取得完了後に呼び出されることを前提とし、
 * 登録済みデータを各プルダウンの初期値としてセットする。
 * - 取得区分・ステータス: asset から取得した値をそのまま v-model にセット
 *   → 選択肢に一致する option が自動選択される
 * - 機器種別: バックエンド未対応のため現時点では未選択（将来対応予定）
 */
function openEdit() {
  if (!asset.value) return
  Object.assign(form, {
    deviceName:          asset.value.deviceName,
    deviceType:          asset.value.deviceType ?? '', // 登録済みの機器種別を初期選択（未設定は未選択）
    acquisitionType:     asset.value.acquisitionType, // 登録済みの取得区分を初期選択
    status:              asset.value.status,           // 登録済みのステータスを初期選択
    maker:               asset.value.maker ?? '',
    modelNumber:         asset.value.modelNumber ?? '',
    serialNumber:        asset.value.serialNumber ?? '',
    hostname:            asset.value.hostname ?? '',
    // location はエージェントが自動設定するため編集フォームにセットしない
    assignedEmployeeId:  asset.value.assignedEmployeeId,
    note:                asset.value.note ?? '',
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
      deviceName:         form.deviceName,
      deviceType:         form.deviceType || undefined,  // 未選択（空文字）は送信しない
      acquisitionType:    form.acquisitionType,
      status:             form.status,
      maker:              form.maker || undefined,
      modelNumber:        form.modelNumber || undefined,
      serialNumber:       form.serialNumber || undefined,
      hostname:           form.hostname || undefined,
      // location はエージェントが自動設定するため更新リクエストに含めない
      assignedEmployeeId: form.assignedEmployeeId,
      note:               form.note || undefined,
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
  // 初期表示時にコードマスタ API からプルダウン選択肢を取得する
  // 編集ボタン押下前に取得完了させることで、登録済みデータを正しく初期選択できる
  loadAcquisitionTypes()
  loadStatusOptions()
  loadDeviceTypes()
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

/* タブバー */
.tab-bar {
  display: flex;
  gap: 0;
  border-bottom: 2px solid #e5e7eb;
}

/* タブボタン共通 */
.tab-btn {
  padding: 10px 20px;
  font-size: 14px;
  font-weight: 500;
  color: #6b7280;
  background: none;
  border: none;
  border-bottom: 2px solid transparent;
  margin-bottom: -2px;  /* border-bottom と重ねてアクティブ線を上書きする */
  cursor: pointer;
  transition: color 0.15s, border-color 0.15s;
}
.tab-btn:hover { color: #374151; }

/* アクティブタブ */
.tab-btn.active {
  color: #6366f1;
  font-weight: 700;
  border-bottom-color: #6366f1;
}

/* プレースホルダーテキスト（データなし・エージェント未導入） */
.placeholder-text {
  color: #9ca3af;
  font-size: 14px;
  text-align: center;
  padding: 32px 0;
}

/* ソフトウェア件数バッジ */
.sw-count {
  font-size: 12px;
  font-weight: 400;
  color: #6b7280;
  margin-left: 8px;
}

/* ソフトウェア一覧テーブル */
.sw-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 13px;
}
.sw-table th {
  background: #f9fafb;
  padding: 8px 12px;
  text-align: left;
  font-weight: 600;
  color: #374151;
  border-bottom: 1px solid #e5e7eb;
}
.sw-table td {
  padding: 8px 12px;
  border-bottom: 1px solid #f3f4f6;
  color: #374151;
}

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
