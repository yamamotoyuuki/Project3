<!--
  機能ID: WEB_RNT0103
  components/WEB_RNT0103_RentalReturnDialog.vue
  -----------------------------------------------
  レンタル返却登録ダイアログ
  -----------------------------------------------
  返却日の入力を伴う業務固有のダイアログ。
  汎用の AppConfirmDialog とは異なり、
  フォームフィールドを持つため独立コンポーネントとして実装する。

  Props:
    modelValue    - 表示フラグ（v-model）
    assetNumber   - 資産番号（ダイアログ内に表示）
    deviceName    - 端末名（任意。ダイアログ内に表示）
    rentalEndDate - 契約終了日（YYYY-MM-DD。返却日の初期値として使用）

  Emits:
    update:modelValue      - ダイアログ閉じるとき false を通知
    confirm(returnDate)    - 「返却する」押下時に返却日（YYYY-MM-DD）を通知
  -----------------------------------------------
-->
<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="overlay"
      role="dialog"
      aria-modal="true"
      @click.self="handleCancel"
    >
      <div class="dialog">

        <!-- タイトル -->
        <h3 class="dialog-title">返却登録</h3>

        <!-- 対象資産の説明 -->
        <p class="dialog-message">
          <!-- 端末名があれば「端末名（資産番号）」、なければ「資産番号」のみ表示 -->
          <span v-if="deviceName">{{ deviceName }}（{{ assetNumber }}）</span>
          <span v-else>{{ assetNumber }}</span>
          の返却を登録します。返却後、情報は編集できなくなります。
        </p>

        <!-- 返却日入力フォーム -->
        <div class="form-group">
          <label class="form-label">
            返却日 <span class="required">*</span>
          </label>
          <!-- 返却日（必須）。契約終了日を初期値とする -->
          <input
            v-model="returnDate"
            type="date"
            class="input"
          />
          <!-- バリデーションエラー -->
          <p v-if="dateError" class="field-error">{{ dateError }}</p>
        </div>

        <!-- アクションボタン行 -->
        <div class="dialog-actions">
          <button class="btn btn-ghost" @click="handleCancel">キャンセル</button>
          <button class="btn btn-warning" @click="handleConfirm">返却する</button>
        </div>

      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

// ---- Props ----
const props = defineProps<{
  /** 表示フラグ（v-model） */
  modelValue:    boolean
  /** 対象資産番号（ダイアログ内に表示する） */
  assetNumber:   string
  /** 端末名（任意。指定時は端末名（資産番号）の形式で表示する） */
  deviceName?:   string
  /** 契約終了日（YYYY-MM-DD 形式）。返却日入力欄のデフォルト値として使用する */
  rentalEndDate: string
}>()

// ---- Emits ----
const emit = defineEmits<{
  /** ダイアログが閉じるときに false を通知する */
  (e: 'update:modelValue', value: boolean): void
  /** 「返却する」押下時に返却日（YYYY-MM-DD 形式）を通知する */
  (e: 'confirm', returnDate: string): void
}>()

// ---- 内部状態 ----

/** 返却日入力値（初期値: 契約終了日） */
const returnDate = ref<string>(props.rentalEndDate)

/** 返却日バリデーションエラーメッセージ */
const dateError = ref<string>('')

// ---- ウォッチャー ----

/**
 * ダイアログが開くたびに返却日を契約終了日にリセットし、
 * バリデーションエラーも初期化する。
 */
watch(() => props.modelValue, (isOpen: boolean) => {
  if (isOpen) {
    returnDate.value = props.rentalEndDate  // 契約終了日をデフォルトにセット
    dateError.value  = ''
  }
})

// ---- イベントハンドラ ----

/** 「返却する」ボタン押下：バリデーション後に親へ返却日を通知する */
function handleConfirm(): void {
  if (!returnDate.value) {
    dateError.value = '返却日を入力してください'
    return
  }
  dateError.value = ''
  emit('confirm', returnDate.value)
  emit('update:modelValue', false)
}

/** キャンセル・オーバーレイクリック：ダイアログを閉じる */
function handleCancel(): void {
  emit('update:modelValue', false)
}

</script>

<style scoped>
/* ---- オーバーレイ ----
   z-index を 2000 にし、他のモーダル（z-index: 1000）の上に表示する */
.overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 2000;
}

/* ---- ダイアログ本体 ---- */
.dialog {
  background: white;
  border-radius: 12px;
  padding: 28px 28px 22px;
  width: 400px;
  max-width: 92vw;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.18);
}

/* タイトル */
.dialog-title {
  font-size: 16px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 12px;
}

/* メインメッセージ */
.dialog-message {
  font-size: 14px;
  color: #374151;
  margin: 0 0 20px;
  line-height: 1.6;
}

/* 返却日フォームグループ */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 5px;
  margin-bottom: 4px;
}

.form-label {
  font-size: 12px;
  font-weight: 600;
  color: #374151;
}

/* 必須マーク */
.required { color: #ef4444; }

/* 日付入力 */
.input {
  height: 36px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 13px;
  outline: none;
  width: 100%;
  box-sizing: border-box;
}
.input:focus { border-color: #6366f1; }

/* フィールドバリデーションエラー */
.field-error {
  font-size: 12px;
  color: #ef4444;
  margin: 0;
}

/* ボタン行（右寄せ） */
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 22px;
}

/* ---- ボタン ---- */
.btn {
  height: 36px;
  padding: 0 18px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 500;
  cursor: pointer;
  border: none;
  transition: opacity 0.15s;
}

/* キャンセル */
.btn-ghost   { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }
.btn-ghost:hover   { background: #f9fafb; }

/* 返却する（オレンジ） */
.btn-warning { background: #f59e0b; color: white; }
.btn-warning:hover { background: #d97706; }
</style>
