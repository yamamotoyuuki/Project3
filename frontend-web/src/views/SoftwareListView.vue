<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <h2 class="page-title">
          <img src="@/image/icon-software.svg" class="page-title-icon" alt="ソフトウェア" />
          ソフトウェアライセンス
        </h2>
        <button v-if="authStore.isItStaff" class="btn btn-primary" @click="openCreate">
          ＋ 登録
        </button>
      </div>

      <div class="search-bar card">
        <input v-model="searchForm.keyword" class="input" placeholder="ソフトウェア名 / 発行元"
          @keyup.enter="doSearch" />
        <label class="checkbox-label">
          <input v-model="searchForm.overLimit" type="checkbox" /> ライセンス超過のみ
        </label>
        <button class="btn btn-secondary" @click="doSearch">
          <img src="@/image/icon-search.svg" class="btn-icon-img" alt="検索" /> 検索
        </button>
        <button class="btn btn-ghost" @click="resetSearch">リセット</button>
      </div>

      <div class="card table-card">
        <div v-if="loading" class="loading">読み込み中…</div>
        <div v-else-if="error" class="error-msg">{{ error }}</div>
        <template v-else>
          <table class="table">
            <thead>
              <tr>
                <th>ソフトウェア名</th>
                <th>発行元</th>
                <th>ライセンス種別</th>
                <th>購入数</th>
                <th>インストール台数</th>
                <th>状態</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="software.length === 0"><td colspan="7" class="empty">データがありません</td></tr>
              <tr v-for="s in software" :key="s.id" :class="{ 'row-over': s.overLimit }">
                <td>{{ s.softwareName }}</td>
                <td class="small-text">{{ s.publisher || '—' }}</td>
                <td class="small-text">{{ s.licenseType || '—' }}</td>
                <td class="num">{{ s.purchasedCount }}</td>
                <td class="num" :class="{ 'text-danger': s.overLimit }">{{ s.installedCount }}</td>
                <td>
                  <!-- imageフォルダの警告アイコン画像を使用（規約準拠） -->
                  <span v-if="s.overLimit" class="badge over">
                    <img src="@/image/icon-warning.svg" class="badge-icon" alt="warning" /> 超過
                  </span>
                  <span v-else-if="s.purchasedCount === 0" class="badge free">フリー</span>
                  <span v-else class="badge ok">正常</span>
                </td>
                <td>
                  <button v-if="authStore.isItStaff" class="btn-icon" @click="openEdit(s)"><img src="@/image/icon-edit.svg" alt="編集" /></button>
                </td>
              </tr>
            </tbody>
          </table>
          <AppPagination v-model="currentPage" :total-pages="totalPages"
            :total-elements="totalElements" :size="pageSize" />
        </template>
      </div>
    </div>

    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h3 class="modal-title">{{ editTarget ? 'ライセンス編集' : 'ライセンス登録' }}</h3>
        <div class="form-grid">
          <div class="form-group full">
            <label>ソフトウェア名 <span class="required">*</span></label>
            <input v-model="form.softwareName" class="input" />
          </div>
          <div class="form-group">
            <label>発行元</label>
            <input v-model="form.publisher" class="input" />
          </div>
          <div class="form-group">
            <label>ライセンス種別</label>
            <input v-model="form.licenseType" class="input" placeholder="例: 永続・年間サブスク" />
          </div>
          <div class="form-group full">
            <label>購入ライセンス数</label>
            <input v-model.number="form.purchasedCount" type="number" min="0" class="input" />
          </div>
          <div class="form-group full">
            <label>備考</label>
            <textarea v-model="form.note" class="textarea" rows="2" />
          </div>
        </div>
        <p v-if="formError" class="form-error">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="closeModal">キャンセル</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveSoftware">
            {{ saving ? '保存中…' : '保存' }}
          </button>
        </div>
      </div>
    </div>
  </AppLayout>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { softwareApi } from '@/api/software'
import AppLayout from '@/components/AppLayout.vue'
import AppPagination from '@/components/AppPagination.vue'
import type { SoftwareLicense } from '@/types'

const authStore = useAuthStore()
const software = ref<SoftwareLicense[]>([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const pageSize = 20
const totalPages = ref(0)
const totalElements = ref(0)

const searchForm = reactive({ keyword: '', overLimit: false })

async function loadSoftware() {
  loading.value = true; error.value = ''
  try {
    const res = await softwareApi.findAll({
      page: currentPage.value, size: pageSize,
      keyword: searchForm.keyword || undefined,
      overLimit: searchForm.overLimit || undefined,
    })
    software.value = res.data.content
    totalPages.value = res.data.totalPages
    totalElements.value = res.data.totalElements
  } catch (e: any) { error.value = e?.response?.data?.message ?? '取得失敗' }
  finally { loading.value = false }
}

function doSearch() { currentPage.value = 0; loadSoftware() }
function resetSearch() { searchForm.keyword = ''; searchForm.overLimit = false; doSearch() }
watch(currentPage, loadSoftware)

const showModal = ref(false)
const saving = ref(false)
const formError = ref('')
const editTarget = ref<SoftwareLicense | null>(null)
const form = reactive({ softwareName: '', publisher: '', licenseType: '', purchasedCount: 0, note: '' })

function openCreate() {
  editTarget.value = null
  Object.assign(form, { softwareName: '', publisher: '', licenseType: '', purchasedCount: 0, note: '' })
  formError.value = ''; showModal.value = true
}
function openEdit(s: SoftwareLicense) {
  editTarget.value = s
  Object.assign(form, { softwareName: s.softwareName, publisher: s.publisher ?? '', licenseType: s.licenseType ?? '', purchasedCount: s.purchasedCount, note: s.note ?? '' })
  formError.value = ''; showModal.value = true
}
function closeModal() { showModal.value = false }

async function saveSoftware() {
  if (!form.softwareName) { formError.value = 'ソフトウェア名は必須です'; return }
  saving.value = true
  try {
    if (editTarget.value) {
      await softwareApi.update(editTarget.value.id, form)
    } else {
      await softwareApi.create(form)
    }
    closeModal(); loadSoftware()
  } catch (e: any) { formError.value = e?.response?.data?.message ?? '保存失敗' }
  finally { saving.value = false }
}

onMounted(loadSoftware)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-size: 20px; font-weight: 700; color: #1a1a2e; margin: 0; }
.search-bar { display: flex; align-items: center; gap: 12px; flex-wrap: wrap; padding: 14px 16px; }
.card { background: white; border-radius: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.07); }
.table-card { padding: 16px; }
.input, .select { height: 36px; border: 1px solid #d1d5db; border-radius: 6px; padding: 0 10px; font-size: 13px; outline: none; min-width: 220px; }
.input:focus { border-color: #6366f1; }
.checkbox-label { display: flex; align-items: center; gap: 6px; font-size: 13px; cursor: pointer; }
.btn { height: 36px; padding: 0 16px; border-radius: 6px; font-size: 13px; cursor: pointer; border: none; font-weight: 500; }
.btn:disabled { opacity: 0.5; }
.btn-primary { background: #6366f1; color: white; }
.btn-primary:hover { background: #4f46e5; }
.btn-secondary { background: #f3f4f6; color: #374151; border: 1px solid #d1d5db; }
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }
.table { width: 100%; border-collapse: collapse; font-size: 13px; }
.table th { background: #f9fafb; padding: 10px 12px; text-align: left; font-weight: 600; color: #374151; border-bottom: 1px solid #e5e7eb; }
.table td { padding: 10px 12px; border-bottom: 1px solid #f3f4f6; }
.empty { text-align: center; color: #9ca3af; padding: 40px; }
.small-text { font-size: 12px; color: #6b7280; }
.num { text-align: right; font-family: monospace; font-weight: 600; }
.text-danger { color: #ef4444; }
.row-over td { background: #fff5f5; }
.badge { display: inline-block; padding: 2px 8px; border-radius: 999px; font-size: 11px; font-weight: 700; }
.badge.ok   { background: #dcfce7; color: #166534; }
.badge.over { background: #fee2e2; color: #991b1b; }
.badge.free { background: #f3f4f6; color: #6b7280; }
.loading { padding: 40px; text-align: center; color: #9ca3af; }
.error-msg { padding: 16px; color: #ef4444; }
.btn-icon { background: none; border: none; cursor: pointer; font-size: 15px; padding: 4px; border-radius: 4px; }
.btn-icon:hover { background: #f3f4f6; }
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.45); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal { background: white; border-radius: 12px; padding: 28px; width: 520px; max-width: 95vw; max-height: 90vh; overflow-y: auto; }
.modal-title { font-size: 18px; font-weight: 700; margin: 0 0 20px; color: #1a1a2e; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group.full { grid-column: 1 / -1; }
.form-group label { font-size: 12px; font-weight: 600; color: #374151; }
.textarea { border: 1px solid #d1d5db; border-radius: 6px; padding: 8px 10px; font-size: 13px; resize: vertical; outline: none; }
.required { color: #ef4444; }
.form-error { color: #ef4444; font-size: 13px; margin-top: 8px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
</style>
