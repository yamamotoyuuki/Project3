<!--
  機能ID: WEB_EMP0101
  views/WEB_EMP0101_EmployeeListView.vue
  -----------------------------------------------
  社員一覧画面コンポーネント
  -----------------------------------------------
-->
<template>
  <AppLayout>
    <div class="page">
      <div class="page-header">
        <h2 class="page-title">
          <img src="@/image/icon-employee.svg" class="page-title-icon" alt="社員管理" />
          社員管理
        </h2>
        <button v-if="authStore.isItStaff" class="btn btn-primary" @click="openCreate">
          ＋ 社員登録
        </button>
      </div>

      <!-- 検索バー -->
      <div class="search-bar card">
        <input
          v-model="searchForm.keyword"
          class="input"
          placeholder="社員コード / 氏名 / 部署"
          @keyup.enter="doSearch"
        />
        <select v-model="searchForm.isActive" class="select">
          <option value="">すべて</option>
          <option value="true">在籍中</option>
          <option value="false">退職済</option>
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
                <th>社員コード</th>
                <th>氏名</th>
                <th>部署</th>
                <th>役職</th>
                <th>メール</th>
                <th>在籍</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="employees.length === 0">
                <td colspan="7" class="empty">データがありません</td>
              </tr>
              <tr v-for="e in employees" :key="e.id">
                <td class="mono">{{ e.employeeCode }}</td>
                <td>{{ e.fullName }}</td>
                <td>{{ e.department || '—' }}</td>
                <td>{{ e.position || '—' }}</td>
                <td class="small-text">{{ e.email || '—' }}</td>
                <td>
                  <span class="active-badge" :class="e.isActive ? 'active' : 'inactive'">
                    {{ e.isActive ? '在籍' : '退職' }}
                  </span>
                </td>
                <td>
                  <button
                    v-if="authStore.isItStaff"
                    class="btn-icon"
                    title="編集"
                    @click="openEdit(e)"
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

    <!-- 登録 / 編集モーダル -->
    <div v-if="showModal" class="modal-overlay" @click.self="closeModal">
      <div class="modal">
        <h3 class="modal-title">{{ editTarget ? '社員編集' : '社員登録' }}</h3>
        <div class="form-grid">
          <!-- 社員コード: 登録・編集どちらでも入力可能（誤入力の訂正を考慮） -->
          <div class="form-group">
            <label>社員コード <span class="required">*</span></label>
            <input
              v-model="form.employeeCode"
              class="input"
              placeholder="例: EMP001"
            />
          </div>
          <div class="form-group">
            <label>氏名 <span class="required">*</span></label>
            <input v-model="form.fullName" class="input" placeholder="例: 山田 太郎" />
          </div>
          <div class="form-group">
            <label>部署</label>
            <input v-model="form.department" class="input" placeholder="例: 情報システム部" />
          </div>
          <div class="form-group">
            <label>役職</label>
            <input v-model="form.position" class="input" placeholder="例: 主任" />
          </div>
          <div class="form-group">
            <label>メールアドレス</label>
            <input v-model="form.email" class="input" type="email" />
          </div>
          <div class="form-group">
            <label>電話番号</label>
            <input v-model="form.phone" class="input" />
          </div>
          <div class="form-group full">
            <label>勤務地</label>
            <input v-model="form.location" class="input" placeholder="例: 東京本社" />
          </div>
          <div v-if="editTarget" class="form-group">
            <label>在籍ステータス</label>
            <select v-model="form.isActive" class="select">
              <option :value="true">在籍中</option>
              <option :value="false">退職</option>
            </select>
          </div>
        </div>
        <p v-if="formError" class="form-error">{{ formError }}</p>
        <div class="modal-actions">
          <button class="btn btn-ghost" @click="closeModal">キャンセル</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveEmployee">
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
import { employeesApi } from '@/api/employees'
import AppLayout from '@/components/AppLayout.vue'
import AppPagination from '@/components/AppPagination.vue'
import type { Employee } from '@/types'

const authStore = useAuthStore()

// ---- 一覧 ----
const employees = ref<Employee[]>([])
const loading = ref(false)
const error = ref('')
const currentPage = ref(0)
const pageSize = 20
const totalPages = ref(0)
const totalElements = ref(0)

const searchForm = reactive({
  keyword: '',
  isActive: '' as '' | 'true' | 'false',
})

async function loadEmployees() {
  loading.value = true
  error.value = ''
  try {
    const res = await employeesApi.findAll({
      page: currentPage.value,
      size: pageSize,
      keyword: searchForm.keyword || undefined,
      isActive: searchForm.isActive === '' ? undefined
               : searchForm.isActive === 'true',
    })
    employees.value = res.data.content
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
  loadEmployees()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.isActive = ''
  currentPage.value = 0
  loadEmployees()
}

watch(currentPage, loadEmployees)

// ---- モーダル ----
const showModal = ref(false)
const saving = ref(false)
const formError = ref('')
const editTarget = ref<Employee | null>(null)

const form = reactive({
  employeeCode: '',
  fullName: '',
  department: '',
  position: '',
  email: '',
  phone: '',
  location: '',
  isActive: true,
})

function openCreate() {
  editTarget.value = null
  Object.assign(form, {
    employeeCode: '', fullName: '', department: '',
    position: '', email: '', phone: '', location: '', isActive: true,
  })
  formError.value = ''
  showModal.value = true
}

function openEdit(e: Employee) {
  editTarget.value = e
  Object.assign(form, {
    employeeCode: e.employeeCode,  // 社員コードを現在値でセット（編集可能）
    fullName:     e.fullName,
    department:   e.department ?? '',
    position:     e.position ?? '',
    email:        e.email ?? '',
    phone:        e.phone ?? '',
    location:     e.location ?? '',
    isActive:     e.isActive,
  })
  formError.value = ''
  showModal.value = true
}

function closeModal() { showModal.value = false }

async function saveEmployee() {
  formError.value = ''
  // 社員コードは登録・編集ともに必須
  if (!form.employeeCode) { formError.value = '社員コードは必須です'; return }
  if (!form.fullName)     { formError.value = '氏名は必須です';       return }
  saving.value = true
  try {
    if (editTarget.value) {
      // 更新: 社員コード変更を含めて送信（バックエンドで重複チェックあり）
      await employeesApi.update(editTarget.value.id, {
        employeeCode: form.employeeCode,
        fullName:     form.fullName,
        department:   form.department || undefined,
        position:     form.position   || undefined,
        email:        form.email      || undefined,
        phone:        form.phone      || undefined,
        location:     form.location   || undefined,
        isActive:     form.isActive,
      })
    } else {
      // 新規登録
      await employeesApi.create({
        employeeCode: form.employeeCode,
        fullName:     form.fullName,
        department:   form.department || undefined,
        position:     form.position   || undefined,
        email:        form.email      || undefined,
        phone:        form.phone      || undefined,
        location:     form.location   || undefined,
      })
    }
    closeModal()
    loadEmployees()
  } catch (e: any) {
    formError.value = e?.response?.data?.message ?? '保存に失敗しました'
  } finally {
    saving.value = false
  }
}

onMounted(loadEmployees)
</script>

<style scoped>
.page { display: flex; flex-direction: column; gap: 16px; }
.page-header { display: flex; justify-content: space-between; align-items: center; }
.page-title { font-size: 20px; font-weight: 700; color: #1a1a2e; margin: 0; }

.search-bar { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; padding: 14px 16px; }
.card { background: white; border-radius: 10px; box-shadow: 0 1px 4px rgba(0,0,0,0.07); }
.table-card { padding: 16px; }

.input, .select {
  height: 36px; border: 1px solid #d1d5db; border-radius: 6px;
  padding: 0 10px; font-size: 13px; outline: none;
}
.input { min-width: 220px; }
.input:focus, .select:focus { border-color: #6366f1; }

.btn {
  height: 36px; padding: 0 16px; border-radius: 6px; font-size: 13px;
  cursor: pointer; border: none; font-weight: 500;
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }
.btn-primary { background: #6366f1; color: white; }
.btn-primary:hover { background: #4f46e5; }
.btn-secondary { background: #f3f4f6; color: #374151; border: 1px solid #d1d5db; }
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }

.table { width: 100%; border-collapse: collapse; font-size: 13px; }
.table th {
  background: #f9fafb; padding: 10px 12px; text-align: left;
  font-weight: 600; color: #374151; border-bottom: 1px solid #e5e7eb;
}
.table td { padding: 10px 12px; border-bottom: 1px solid #f3f4f6; }
.empty { text-align: center; color: #9ca3af; padding: 40px; }
.mono { font-family: monospace; }
.small-text { font-size: 12px; color: #6b7280; }

.active-badge {
  display: inline-block; padding: 2px 10px; border-radius: 999px;
  font-size: 12px; font-weight: 600;
}
.active   { background: #dcfce7; color: #166534; }
.inactive { background: #f3f4f6; color: #6b7280; }

.btn-icon {
  background: none; border: none; cursor: pointer;
  font-size: 16px; padding: 4px; border-radius: 4px;
}
.btn-icon:hover { background: #f3f4f6; }
.loading { padding: 40px; text-align: center; color: #9ca3af; }
.error-msg { padding: 16px; color: #ef4444; }

.modal-overlay {
  position: fixed; inset: 0; background: rgba(0,0,0,0.45);
  display: flex; align-items: center; justify-content: center; z-index: 1000;
}
.modal {
  background: white; border-radius: 12px; padding: 28px;
  width: 560px; max-width: 95vw; max-height: 90vh; overflow-y: auto;
}
.modal-title { font-size: 18px; font-weight: 700; margin: 0 0 20px; color: #1a1a2e; }
.form-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 14px; }
.form-group { display: flex; flex-direction: column; gap: 5px; }
.form-group.full { grid-column: 1 / -1; }
.form-group label { font-size: 12px; font-weight: 600; color: #374151; }
.required { color: #ef4444; }
.form-error { color: #ef4444; font-size: 13px; margin-top: 8px; }
.modal-actions { display: flex; justify-content: flex-end; gap: 10px; margin-top: 24px; }
</style>
