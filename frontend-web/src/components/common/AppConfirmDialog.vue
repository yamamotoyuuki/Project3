<!--
  components/AppConfirmDialog.vue
  -----------------------------------------------
  アプリ共通 確認ダイアログコンポーネント
  -----------------------------------------------
  ブラウザネイティブの confirm() の代替として使用する。
  モーダル内から呼び出す場合も z-index により前面に表示される。

  使用例:
    <AppConfirmDialog
      v-model="showDialog"
      message="削除します。よろしいですか？"
      sub-message="この操作は取り消せません。"
      confirm-label="削除する"
      variant="danger"
      @confirm="onConfirm"
    />

  Props:
    modelValue    - 表示フラグ（v-model）
    title         - ダイアログタイトル（省略時: "確認"）
    message       - メインメッセージ（必須）
    subMessage    - サブメッセージ（任意。補足説明に使用）
    confirmLabel  - 確認ボタンのラベル（省略時: "確認する"）
    cancelLabel   - キャンセルボタンのラベル（省略時: "キャンセル"）
    variant       - 確認ボタンの色調（"primary" / "danger" / "warning"）

  Emits:
    update:modelValue - 閉じるときに false を通知（v-model 更新）
    confirm           - 確認ボタン押下時
  -----------------------------------------------
-->
<template>
  <Teleport to="body">
    <div
      v-if="modelValue"
      class="overlay"
      role="alertdialog"
      aria-modal="true"
      @click.self="handleCancel"
    >
      <div class="dialog">
        <!-- ダイアログタイトル -->
        <h3 class="dialog-title">{{ title }}</h3>

        <!-- メインメッセージ -->
        <p class="dialog-message">{{ message }}</p>

        <!-- サブメッセージ（任意） -->
        <p v-if="subMessage" class="dialog-sub">{{ subMessage }}</p>

        <!-- アクションボタン行 -->
        <div class="dialog-actions">
          <button class="btn btn-ghost" @click="handleCancel">
            {{ cancelLabel }}
          </button>
          <button class="btn" :class="confirmBtnClass" @click="handleConfirm">
            {{ confirmLabel }}
          </button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { computed } from 'vue'

// ---- Props ----
const props = withDefaults(defineProps<{
  /** 表示フラグ（v-model） */
  modelValue:   boolean
  /** ダイアログタイトル */
  title?:       string
  /** メインメッセージ（必須） */
  message:      string
  /** サブメッセージ（補足説明。任意） */
  subMessage?:  string
  /** 確認ボタンのラベル */
  confirmLabel?: string
  /** キャンセルボタンのラベル */
  cancelLabel?:  string
  /**
   * 確認ボタンの色調
   * primary（インジゴ）/ danger（赤）/ warning（オレンジ）
   */
  variant?: 'primary' | 'danger' | 'warning'
}>(), {
  title:        '確認',
  confirmLabel: '確認する',
  cancelLabel:  'キャンセル',
  variant:      'primary',
})

// ---- Emits ----
const emit = defineEmits<{
  /** ダイアログが閉じるときに false を通知する（v-model 更新） */
  (e: 'update:modelValue', value: boolean): void
  /** 確認ボタン押下時 */
  (e: 'confirm'): void
}>()

// ---- 算出プロパティ ----

/** variant に応じた確認ボタンのCSSクラスを返す */
const confirmBtnClass = computed((): string => {
  switch (props.variant) {
    case 'danger':  return 'btn-danger'
    case 'warning': return 'btn-warning'
    default:        return 'btn-primary'
  }
})

// ---- イベントハンドラ ----

/** 確認ボタン押下：親に confirm を通知してダイアログを閉じる */
function handleConfirm(): void {
  emit('confirm')
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
  margin: 0 0 14px;
}

/* メインメッセージ */
.dialog-message {
  font-size: 14px;
  color: #374151;
  margin: 0 0 6px;
  line-height: 1.6;
}

/* サブメッセージ（補足・注意書き） */
.dialog-sub {
  font-size: 12px;
  color: #9ca3af;
  margin: 0 0 20px;
}

/* ボタン行（右寄せ） */
.dialog-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 22px;
}

/* ---- ボタン共通 ---- */
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
.btn:disabled { opacity: 0.5; cursor: not-allowed; }

/* キャンセル */
.btn-ghost   { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }
.btn-ghost:hover   { background: #f9fafb; }

/* primary（インジゴ） */
.btn-primary { background: #6366f1; color: white; }
.btn-primary:hover { background: #4f46e5; }

/* danger（赤） */
.btn-danger  { background: #ef4444; color: white; }
.btn-danger:hover  { background: #dc2626; }

/* warning（オレンジ） */
.btn-warning { background: #f59e0b; color: white; }
.btn-warning:hover { background: #d97706; }
</style>
