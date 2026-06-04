<!--
  components/MultiSelectFilter.vue
  -----------------------------------------------
  複数選択フィルタ 共通コンポーネント
  -----------------------------------------------
  機器種別・ステータス・取得区分など、コードマスタから動的に取得した
  選択肢を複数チェックボックスで絞り込むための汎用コンポーネント。
  チェックした項目を「含む」条件としてフィルタする。

  使用方法:
    <MultiSelectFilter
      label="ステータス"
      :options="statusOptions"
      v-model="searchForm.statusFilter"
    />

  Props:
    label      - フィルタのラベル名（例: "ステータス"）
    options    - 選択肢リスト（CodeValue[]）
    modelValue - 選択状態 { values: string[] }

  Emits:
    update:modelValue - 選択状態変更時に親へ通知する
-->
<template>
  <div class="msf" ref="wrapperRef">

    <!-- トリガーボタン（クリックでドロップダウンを開閉） -->
    <button
      type="button"
      class="msf-trigger"
      :class="{ 'msf-trigger--active': isActiveFilter }"
      @click="toggleOpen"
    >
      {{ label }}：{{ triggerLabel }}
      <!-- 開閉状態を示す三角矢印 -->
      <span class="msf-caret">{{ isOpen ? '▲' : '▼' }}</span>
    </button>

    <!-- ドロップダウンパネル（isOpen が true のときのみ表示） -->
    <div v-if="isOpen" class="msf-panel">

      <!-- すべて選択チェックボックス（先頭に固定） -->
      <label class="msf-item msf-item--all">
        <input
          type="checkbox"
          :checked="isAllSelected"
          @change="toggleAll"
        />
        すべて選択
      </label>

      <hr class="msf-hr" />

      <!-- 各選択肢チェックボックス（コードマスタから動的生成） -->
      <label
        v-for="opt in options"
        :key="opt.codeValue"
        class="msf-item"
      >
        <input
          type="checkbox"
          :checked="isChecked(opt.codeValue)"
          @change="toggleItem(opt.codeValue)"
        />
        {{ opt.codeLabel }}
      </label>

    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import type { CodeValue, MultiFilterValue } from '@/types'

// ---- Props / Emits ----

const props = defineProps<{
  /** フィルタのラベル名（例: "ステータス"） */
  label:      string
  /** 選択肢リスト（コードマスタから取得した CodeValue[]） */
  options:    CodeValue[]
  /** 親から受け取る現在の選択状態 */
  modelValue: MultiFilterValue
}>()

const emit = defineEmits<{
  /** 選択状態が変化したときに親へ通知する */
  (e: 'update:modelValue', value: MultiFilterValue): void
}>()

// ---- 内部状態 ----

/** ドロップダウンの開閉状態 */
const isOpen = ref(false)

/** クリック外検知のためのルート要素参照 */
const wrapperRef = ref<HTMLElement | null>(null)

// ---- 算出プロパティ ----

/**
 * すべての選択肢が選択済みかどうか。
 * 「すべて選択」チェックボックスのチェック状態判定に使用する。
 */
const isAllSelected = computed((): boolean =>
  props.options.length > 0 &&
  props.modelValue.values.length === props.options.length
)

/**
 * フィルタが実際に絞り込みを発生させる状態かどうか。
 * 未選択（空配列）または全選択の場合はフィルタなしとみなす。
 * トリガーボタンのハイライト表示制御に使用する。
 */
const isActiveFilter = computed((): boolean =>
  props.modelValue.values.length > 0 &&
  props.modelValue.values.length < props.options.length
)

/**
 * トリガーボタンに表示する状態ラベル。
 * フィルタなし → "すべて"
 * 絞り込み中   → "X件選択"
 */
const triggerLabel = computed((): string => {
  if (!isActiveFilter.value) return 'すべて'
  return `${props.modelValue.values.length}件選択`
})

// ---- 操作関数 ----

/** ドロップダウンの開閉を切り替える */
function toggleOpen(): void {
  isOpen.value = !isOpen.value
}

/**
 * 指定したコード値が選択済みかどうかを返す。
 * @param codeValue - 判定対象のコード値
 */
function isChecked(codeValue: string): boolean {
  return props.modelValue.values.includes(codeValue)
}

/**
 * 「すべて選択」チェックボックスのトグル処理。
 * 全選択状態 → 全解除（空配列 = フィルタなし）
 * 部分選択 / 未選択 → 全選択
 */
function toggleAll(): void {
  if (isAllSelected.value) {
    // 全選択 → 全解除（フィルタなし）
    emit('update:modelValue', { values: [] })
  } else {
    // 部分選択または未選択 → 全選択
    emit('update:modelValue', { values: props.options.map((o) => o.codeValue) })
  }
}

/**
 * 個別項目のチェック / アンチェック処理。
 * 選択済みの場合は除去、未選択の場合は追加する。
 * @param codeValue - 操作対象のコード値
 */
function toggleItem(codeValue: string): void {
  const current = props.modelValue.values
  const newValues = current.includes(codeValue)
    ? current.filter((v) => v !== codeValue)   // 選択済み → 除去
    : [...current, codeValue]                  // 未選択   → 追加
  emit('update:modelValue', { values: newValues })
}

// ---- ドロップダウン外クリックで閉じる ----

/**
 * ドロップダウン外をクリックした場合にパネルを閉じる。
 * @param e - マウスダウンイベント
 */
function handleOutsideClick(e: MouseEvent): void {
  if (wrapperRef.value && !wrapperRef.value.contains(e.target as Node)) {
    isOpen.value = false
  }
}

onMounted(()  => document.addEventListener('mousedown', handleOutsideClick))
onUnmounted(() => document.removeEventListener('mousedown', handleOutsideClick))
</script>

<style scoped>
/* ---- ラッパー ---- */
.msf {
  position: relative;
  display: inline-block;
}

/* ---- トリガーボタン（通常状態は select と同じ見た目） ---- */
.msf-trigger {
  height: 36px;
  padding: 0 10px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  background: white;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 6px;
  white-space: nowrap;
  outline: none;
}
.msf-trigger:focus { border-color: #6366f1; }

/* フィルタ適用中はアクセントカラーでハイライト表示 */
.msf-trigger--active {
  border-color: #6366f1;
  color: #6366f1;
  background: #f0f0ff;
}

/* 開閉矢印 */
.msf-caret {
  font-size: 10px;
  color: #9ca3af;
  margin-left: 2px;
}

/* ---- ドロップダウンパネル ---- */
.msf-panel {
  position: absolute;
  top: calc(100% + 4px);
  left: 0;
  z-index: 200;
  background: white;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  min-width: 180px;
  padding: 8px 0;
}

/* ---- 区切り線 ---- */
.msf-hr {
  border: none;
  border-top: 1px solid #f3f4f6;
  margin: 4px 0;
}

/* ---- チェックボックス項目 ---- */
.msf-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  font-size: 13px;
  color: #374151;
  cursor: pointer;
  user-select: none;
}
.msf-item:hover { background: #f9fafb; }

/* 「すべて選択」は太字で強調 */
.msf-item--all {
  font-weight: 600;
}
</style>
