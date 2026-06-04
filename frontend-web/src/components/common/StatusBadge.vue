<template>
  <span class="badge" :class="badgeClass">{{ label }}</span>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { PcStatus } from '@/types'
import { PC_STATUS_LABELS } from '@/types'

const props = defineProps<{ status: PcStatus }>()

const label = computed(() => PC_STATUS_LABELS[props.status] ?? props.status)

const badgeClass = computed(() => ({
  'badge-in-use':     props.status === 'IN_USE',
  'badge-in-storage': props.status === 'IN_STORAGE',
  'badge-disposed':   props.status === 'DISPOSED',
  'badge-in-repair':  props.status === 'IN_REPAIR',
  'badge-returned':   props.status === 'RETURNED',
}))
</script>

<style scoped>
.badge {
  display: inline-block;
  padding: 2px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}
.badge-in-use     { background: #dcfce7; color: #166534; }
.badge-in-storage { background: #dbeafe; color: #1e40af; }
.badge-disposed   { background: #f3f4f6; color: #6b7280; }
.badge-in-repair  { background: #fef3c7; color: #92400e; }
.badge-returned   { background: #ede9fe; color: #5b21b6; }
</style>
