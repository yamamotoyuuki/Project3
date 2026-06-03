<!--
  機能ID: WEB_RNT0102
  components/WEB_RNT0102_RentalDetailModal.vue
  -----------------------------------------------
  レンタル契約詳細・編集ポップアップ

  表示項目（読み取り専用）:
    - 資産番号 / ホスト名 / 残日数

  編集可能項目（IT_STAFF のみ）:
    - ベンダー / 契約番号 / 開始日 / 終了日 / 月額

  emit:
    - close          : 閉じるボタン・オーバーレイクリック時
    - update(req)    : 更新ボタン押下後、バリデーション通過時
    - return         : 返却登録ボタン押下時（未返却 + IT_STAFF のみ表示）
  -----------------------------------------------
-->
<template>
  <!-- モーダルオーバーレイ（外側クリックで閉じる） -->
  <div class="modal-overlay" @click.self="$emit('close')">
    <div class="modal">
      <!-- タイトル行 -->
      <div class="modal-header">
        <h3 class="modal-title">レンタル契約詳細</h3>
        <button class="btn-close" @click="$emit('close')">×</button>
      </div>

      <!-- 読み取り専用情報グリッド -->
      <div class="info-grid">
        <div class="info-row">
          <span class="info-label">資産番号</span>
          <span class="mono">{{ rental.assetNumber }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">ホスト名</span>
          <span>{{ rental.hostname || rental.deviceName || '—' }}</span>
        </div>
        <div class="info-row">
          <span class="info-label">残日数</span>
          <!-- 返却済み / 期限切れ / 残日数 で表示を切り替える -->
          <span v-if="rental.returned" class="text-muted small-text">返却済</span>
          <span v-else-if="rental.expired" class="text-danger font-bold">期限切れ</span>
          <span v-else-if="rental.daysUntilExpiry !== null"
                :class="daysClass">{{ rental.daysUntilExpiry }}日</span>
          <span v-else>—</span>
        </div>
      </div>

      <!-- セクション区切り線 -->
      <div class="divider"></div>

      <!-- 編集可能フォーム（ベンダー・契約番号・開始日・終了日・月額） -->
      <div class="form-grid">
        <!-- ベンダー（必須） -->
        <div class="form-group full">
          <label>ベンダー <span class="required">*</span></label>
          <select v-model="form.rentalVendorId" class="select">
            <option :value="null">選択してください</option>
            <option v-for="v in vendors" :key="v.id" :value="v.id">
              {{ v.companyName }}
            </option>
          </select>
        </div>
        <!-- 契約番号（任意） -->
        <div class="form-group full">
          <label>契約番号</label>
          <input v-model="form.contractNumber" class="input" placeholder="未設定" />
        </div>
        <!-- 開始日（必須） -->
        <div class="form-group">
          <label>開始日 <span class="required">*</span></label>
          <input v-model="form.rentalStartDate" type="date" class="input" />
        </div>
        <!-- 終了日（必須） -->
        <div class="form-group">
          <label>終了日 <span class="required">*</span></label>
          <input v-model="form.rentalEndDate" type="date" class="input" />
        </div>
        <!-- 月額（任意） -->
        <div class="form-group full">
          <label>月額（円）</label>
          <input v-model.number="form.monthlyFee" type="number" min="0"
                 class="input" placeholder="未設定" />
        </div>
      </div>

      <!-- バリデーションエラー表示 -->
      <p v-if="formError" class="form-error">{{ formError }}</p>

      <!-- アクションボタン行 -->
      <div class="modal-actions">
        <!-- 返却登録ボタン（未返却かつ IT_STAFF のみ表示） -->
        <button
          v-if="!rental.returned && isItStaff"
          class="btn btn-warning"
          @click="$emit('return')"
        >返却登録</button>
        <!-- 右側のボタングループ（キャンセル・更新） -->
        <div class="right-actions">
          <button class="btn btn-ghost" @click="$emit('close')">キャンセル</button>
          <button
            v-if="isItStaff"
            class="btn btn-primary"
            :disabled="saving"
            @click="submit"
          >{{ saving ? '更新中…' : '更新' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * RentalDetailModal のスクリプト
 *
 * - Props で受け取った rental の値をフォームの初期値として設定する
 * - rental が切り替わるたびに watch でフォームをリセットする
 * - 更新ボタン押下時にバリデーションを行い、親に update イベントを emit する
 */
import { reactive, ref, computed, watch } from 'vue'
import type { PcRental, RentalVendor, RentalUpdateRequest } from '@/types'

// ---- 型定義 ----

/** コンポーネントの Props 型 */
interface Props {
  rental:    PcRental        // 表示・編集対象のレンタル契約
  vendors:   RentalVendor[]  // ベンダー選択肢（親から渡す）
  saving:    boolean         // 更新中フラグ（ボタン disabled 制御に使用）
  isItStaff: boolean         // IT_STAFF 以上のロールか（編集・返却ボタン表示制御）
}

const props = defineProps<Props>()

/** 親コンポーネントへのイベント定義 */
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update', req: RentalUpdateRequest): void
  (e: 'return'): void
}>()

// ---- フォーム状態 ----

/** バリデーションエラーメッセージ */
const formError = ref('')

/** 編集フォーム（rental.xxx の初期値をセット） */
const form = reactive<RentalUpdateRequest>({
  rentalVendorId:  props.rental.rentalVendorId,
  contractNumber:  props.rental.contractNumber  ?? '',
  rentalStartDate: props.rental.rentalStartDate,
  rentalEndDate:   props.rental.rentalEndDate,
  monthlyFee:      props.rental.monthlyFee      ?? null,
})

/**
 * 表示対象の rental が切り替わったときにフォームをリセットする
 * （同じ親コンポーネントでモーダルを再利用するケースに対応）
 */
watch(() => props.rental, (r) => {
  form.rentalVendorId  = r.rentalVendorId
  form.contractNumber  = r.contractNumber  ?? ''
  form.rentalStartDate = r.rentalStartDate
  form.rentalEndDate   = r.rentalEndDate
  form.monthlyFee      = r.monthlyFee      ?? null
  formError.value      = ''
})

// ---- 計算プロパティ ----

/**
 * 残日数の文字色クラスを返す
 * 30 日以内: 赤 / 90 日以内: オレンジ / それ以外: 標準色
 */
const daysClass = computed(() => {
  const d = props.rental.daysUntilExpiry
  if (d === null) return ''
  if (d <= 30) return 'text-danger'
  if (d <= 90) return 'text-warning'
  return ''
})

// ---- イベントハンドラ ----

/**
 * バリデーションを実行し、問題なければ親に update イベントを emit する
 */
function submit() {
  if (!form.rentalVendorId) {
    formError.value = 'ベンダーを選択してください'
    return
  }
  if (!form.rentalStartDate || !form.rentalEndDate) {
    formError.value = '開始日と終了日を入力してください'
    return
  }
  formError.value = ''
  // スプレッドでリアクティブ参照をコピーして親に渡す
  emit('update', { ...form })
}
</script>

<style scoped>
/* ==============================
   モーダルオーバーレイ・本体
   ============================== */

/* 半透明オーバーレイ（画面全体を覆う） */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* モーダル本体（白カード） */
.modal {
  background: white;
  border-radius: 12px;
  padding: 28px;
  width: 520px;
  max-width: 95vw;
  max-height: 90vh;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 0;
}

/* タイトル行（タイトル左・閉じるボタン右） */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

/* モーダルタイトル */
.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

/* ×閉じるボタン */
.btn-close {
  background: none;
  border: none;
  font-size: 20px;
  color: #9ca3af;
  cursor: pointer;
  padding: 0 4px;
  line-height: 1;
}
.btn-close:hover { color: #374151; }

/* ==============================
   読み取り専用情報グリッド
   ============================== */

.info-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

/* 1行: ラベル左・値右 */
.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 6px;
  padding: 8px 12px;
  font-size: 13px;
}

/* 項目ラベル（グレー） */
.info-label {
  color: #6b7280;
  font-weight: 500;
}

/* 区切り線 */
.divider {
  border-top: 1px solid #e5e7eb;
  margin: 4px 0 16px;
}

/* ==============================
   編集フォーム
   ============================== */

/* 2カラムグリッド */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 4px;
}

/* 1カラム幅フィールド */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

/* 2カラム全幅 */
.form-group.full { grid-column: 1 / -1; }

/* フィールドラベル */
.form-group label {
  font-size: 12px;
  font-weight: 600;
  color: #374151;
}

/* 必須マーク */
.required { color: #ef4444; }

/* テキスト入力 / 日付入力 / セレクト */
.input, .select {
  height: 36px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 13px;
  outline: none;
}
.input:focus, .select:focus { border-color: #6366f1; }

/* バリデーションエラーメッセージ */
.form-error {
  color: #ef4444;
  font-size: 13px;
  margin-top: 8px;
}

/* ==============================
   アクションボタン行
   ============================== */

/* 左側に返却ボタン、右側にキャンセル・更新 */
.modal-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  gap: 10px;
}

/* 右側ボタングループ */
.right-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

/* 共通ボタンスタイル */
.btn {
  height: 36px;
  padding: 0 18px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: none;
}
.btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* 更新ボタン（インジゴ） */
.btn-primary   { background: #6366f1; color: white; }
.btn-primary:hover:not(:disabled) { background: #4f46e5; }

/* キャンセルボタン（透明） */
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }
.btn-ghost:hover { background: #f9fafb; }

/* 返却登録ボタン（オレンジ系） */
.btn-warning { background: #f59e0b; color: white; }
.btn-warning:hover { background: #d97706; }

/* ==============================
   テキストユーティリティ
   ============================== */
.mono       { font-family: monospace; }
.small-text { font-size: 12px; }
.text-muted  { color: #9ca3af; }
.text-danger { color: #ef4444; }
.text-warning { color: #f59e0b; }
.font-bold  { font-weight: 700; }
</style>
