<template>
  <div v-if="totalPages > 1" class="pagination">
    <button
      class="page-btn"
      :disabled="modelValue === 0"
      @click="emit('update:modelValue', modelValue - 1)"
    >
      ‹
    </button>

    <button
      v-for="p in visiblePages"
      :key="p"
      class="page-btn"
      :class="{ active: p === modelValue }"
      @click="emit('update:modelValue', p)"
    >
      {{ p + 1 }}
    </button>

    <button
      class="page-btn"
      :disabled="modelValue >= totalPages - 1"
      @click="emit('update:modelValue', modelValue + 1)"
    >
      ›
    </button>

    <span class="page-info">
      {{ totalElements }} 件中 {{ startItem }}〜{{ endItem }} 件表示
    </span>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  modelValue: number   // current page (0-indexed)
  totalPages: number
  totalElements: number
  size: number
}>()

const emit = defineEmits<{
  'update:modelValue': [page: number]
}>()

const startItem = computed(() => props.modelValue * props.size + 1)
const endItem = computed(() =>
  Math.min((props.modelValue + 1) * props.size, props.totalElements)
)

const visiblePages = computed(() => {
  const pages: number[] = []
  const start = Math.max(0, props.modelValue - 2)
  const end = Math.min(props.totalPages - 1, start + 4)
  for (let i = start; i <= end; i++) pages.push(i)
  return pages
})
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 16px;
  justify-content: flex-end;
}
.page-btn {
  min-width: 32px;
  height: 32px;
  border: 1px solid #e5e7eb;
  background: white;
  border-radius: 6px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.15s;
}
.page-btn:hover:not(:disabled) { background: #f3f4f6; }
.page-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.page-btn.active {
  background: #6366f1;
  border-color: #6366f1;
  color: white;
}
.page-info {
  margin-left: 12px;
  font-size: 12px;
  color: #6b7280;
}
</style>
