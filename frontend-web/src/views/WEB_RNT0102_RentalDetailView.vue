<!--
  機能ID: WEB_RNT0102
  views/WEB_RNT0102_RentalDetailView.vue
  -----------------------------------------------
  レンタル契約詳細・編集ポップアップ（横長2カラム構成）

  左カラム - 詳細・編集フォーム:
    読み取り専用: 資産番号 / ホスト名 / 残日数
    編集可能（IT_STAFF のみ）: ベンダー / 契約番号 / 開始日 / 終了日 / 月額

  右カラム - 履歴パネル:
    変更履歴: 登録日時・最終更新日時（将来は専用 API で詳細表示予定）
    返却履歴: 返却登録状況
    月額推移: 現在の月額（将来は専用 API で変遷表示予定）

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

      <!-- ========== ヘッダー（全幅） ========== -->
      <div class="modal-header">
        <div class="modal-title-row">
          <h3 class="modal-title">レンタル契約詳細</h3>
          <!-- 返却済みの場合、タイトル横に編集不可バッジを表示 -->
          <span v-if="rental.returned" class="readonly-badge">返却済み・編集不可</span>
        </div>
        <button class="btn-close" @click="$emit('close')">×</button>
      </div>

      <!-- ========== ボディ（左：詳細 | 右：履歴） ========== -->
      <div class="modal-body">

        <!-- ===== 左カラム：詳細・編集フォーム ===== -->
        <div class="detail-col">

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
              <span v-else-if="rental.daysUntilExpiry !== null" :class="daysClass">
                {{ rental.daysUntilExpiry }}日
              </span>
              <span v-else>—</span>
            </div>
          </div>

          <!-- セクション区切り線 -->
          <div class="divider" />

          <!-- 編集フォーム（返却済みの場合は全フィールドを disabled にする） -->
          <div class="form-grid">
            <!-- ベンダー（必須） -->
            <div class="form-group full">
              <label>ベンダー <span v-if="!rental.returned" class="required">*</span></label>
              <select
                v-model="form.rentalVendorId"
                class="select"
                :disabled="rental.returned"
              >
                <option :value="null">選択してください</option>
                <option v-for="v in vendors" :key="v.id" :value="v.id">
                  {{ v.companyName }}
                </option>
              </select>
            </div>
            <!-- 契約番号（任意） -->
            <div class="form-group full">
              <label>契約番号</label>
              <input
                v-model="form.contractNumber"
                class="input"
                placeholder="未設定"
                :disabled="rental.returned"
              />
            </div>
            <!-- 開始日（必須） -->
            <div class="form-group">
              <label>契約開始日 <span v-if="!rental.returned" class="required">*</span></label>
              <input
                v-model="form.rentalStartDate"
                type="date"
                class="input"
                :disabled="rental.returned"
              />
            </div>
            <!-- 終了日（必須） -->
            <div class="form-group">
              <label>契約終了日 <span v-if="!rental.returned" class="required">*</span></label>
              <input
                v-model="form.rentalEndDate"
                type="date"
                class="input"
                :disabled="rental.returned"
              />
            </div>
            <!-- 月額（任意） -->
            <div class="form-group full">
              <label>月額（円）</label>
              <input
                v-model.number="form.monthlyFee"
                type="number"
                min="0"
                class="input"
                placeholder="未設定"
                :disabled="rental.returned"
              />
            </div>
          </div>

          <!-- バリデーションエラー表示 -->
          <p v-if="formError" class="form-error">{{ formError }}</p>

          <!-- アクションボタン行 -->
          <div class="modal-actions">
            <!-- 返却登録ボタン（未返却かつ IT_STAFF のみ表示。確認ダイアログを経由する） -->
            <button
              v-if="!rental.returned && isItStaff"
              class="btn btn-warning"
              @click="openReturnConfirm"
            >返却登録</button>
            <!-- 右側のボタングループ（キャンセル・更新） -->
            <div class="right-actions">
              <button class="btn btn-ghost" @click="$emit('close')">閉じる</button>
              <!-- 返却済みの場合は更新ボタンを非表示にする -->
              <button
                v-if="isItStaff && !rental.returned"
                class="btn btn-primary"
                :disabled="saving"
                @click="submit"
              >{{ saving ? '更新中…' : '更新' }}</button>
            </div>
          </div>
        </div>

        <!-- ===== 縦区切り線 ===== -->
        <div class="col-divider" />

        <!-- ===== 右カラム：履歴パネル ===== -->
        <div class="history-col">
          <p class="history-panel-title">変更履歴</p>

          <!-- 読み込み中 -->
          <div v-if="historiesLoading" class="history-loading">読み込み中…</div>

          <!-- 履歴なし -->
          <p v-else-if="historyGroups.length === 0" class="history-empty">
            履歴がありません
          </p>

          <!-- 履歴タイムライン（operation_id でグルーピング、新しい順） -->
          <ul v-else class="history-timeline">
            <li
              v-for="group in historyGroups"
              :key="group.operationId"
              class="history-group"
            >
              <!-- グループヘッダー：操作種別・日時・操作者 -->
              <div class="history-group-header">
                <span class="history-op-badge" :class="`op-${group.operation.toLowerCase()}`">
                  {{ operationLabel(group.operation) }}
                </span>
                <span class="history-date">{{ formatDateTime(group.changedAt) }}</span>
                <span v-if="group.changedByName" class="history-user">
                  {{ group.changedByName }}
                </span>
              </div>

              <!-- フィールド差分リスト（UPDATE 時のみ） -->
              <ul v-if="group.changes.length > 0" class="history-changes">
                <li
                  v-for="change in group.changes"
                  :key="change.id"
                  class="history-change"
                >
                  <span class="change-label">{{ change.fieldLabel }}</span>
                  <span class="change-values">
                    <span class="change-old">{{ change.oldValue ?? '未設定' }}</span>
                    <span class="change-arrow">→</span>
                    <span class="change-new">{{ change.newValue ?? '未設定' }}</span>
                  </span>
                </li>
              </ul>
            </li>
          </ul>
        </div>

      </div><!-- /.modal-body -->
    </div><!-- /.modal -->
  </div><!-- /.modal-overlay -->

  <!-- 返却登録ダイアログ（返却日入力あり専用コンポーネント） -->
  <WEB_RNT0103_RentalReturnDialog
    v-model="showReturnConfirm"
    :asset-number="rental.assetNumber"
    :device-name="rental.deviceName || undefined"
    :rental-end-date="rental.rentalEndDate"
    @confirm="onConfirmReturn"
  />
</template>

<script setup lang="ts">
/**
 * RentalDetailView のスクリプト
 *
 * - Props で受け取った rental の値をフォームの初期値として設定する
 * - rental が切り替わるたびに watch でフォームをリセットする
 * - 更新ボタン押下時にバリデーションを行い、親に update イベントを emit する
 */
import { reactive, ref, computed, watch } from 'vue'
import type { PcRental, RentalVendor, RentalUpdateRequest, RentalHistoryEntry } from '@/types'
import WEB_RNT0103_RentalReturnDialog from '@/components/RNT/WEB_RNT0103_RentalReturnDialog.vue'

// ---- 型定義 ----

/** コンポーネントの Props 型 */
interface Props {
  rental:           PcRental              // 表示・編集対象のレンタル契約
  vendors:          RentalVendor[]        // ベンダー選択肢（親から渡す）
  saving:           boolean               // 更新中フラグ（ボタン disabled 制御に使用）
  isItStaff:        boolean               // IT_STAFF 以上のロールか（編集・返却ボタン表示制御）
  histories:        RentalHistoryEntry[]  // 変更履歴（親の openDetail 時に取得済み）
  historiesLoading: boolean               // 変更履歴の読み込み中フラグ
}

const props = defineProps<Props>()

/** 親コンポーネントへのイベント定義 */
const emit = defineEmits<{
  (e: 'close'): void
  (e: 'update', req: RentalUpdateRequest): void
  (e: 'return', returnDate: string): void
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

// ---- 変更履歴 ----

/**
 * operation_id でグルーピングした履歴グループの型
 * 1グループ = 1回の保存操作
 */
interface HistoryGroup {
  operationId:   string
  operation:     string
  changedByName: string | null
  changedAt:     string
  changes:       RentalHistoryEntry[]  // フィールド差分（UPDATE のみ存在）
}

/**
 * operation_id でグルーピングした履歴グループ。
 * computed では shallowReactive な props 配列の変化を追跡できないケースがあるため、
 * watch + ref で明示的に再構築する。
 */
const historyGroups = ref<HistoryGroup[]>([])

/**
 * props.histories が変わるたびにグルーピングを再構築する。
 * immediate: true でコンポーネント生成時にも即実行する。
 */
watch(
  () => props.histories,
  (entries) => {
    const map = new Map<string, HistoryGroup>()
    for (const entry of entries) {
      if (!map.has(entry.operationId)) {
        map.set(entry.operationId, {
          operationId:   entry.operationId,
          operation:     entry.operation,
          changedByName: entry.changedByName,
          changedAt:     entry.changedAt,
          changes:       [],
        })
      }
      // フィールド差分がある場合のみ changes に追加（CREATE は fieldLabel が null）
      if (entry.fieldLabel) {
        map.get(entry.operationId)!.changes.push(entry)
      }
    }
    // 新しい順（changedAt 降順）にソート
    historyGroups.value = Array.from(map.values()).sort(
      (a, b) => new Date(b.changedAt).getTime() - new Date(a.changedAt).getTime()
    )
  },
  { immediate: true },
)

/**
 * 操作種別コードを日本語ラベルに変換する
 * @param op - 'CREATE' / 'UPDATE' / 'RETURN'
 */
function operationLabel(op: string): string {
  switch (op) {
    case 'CREATE': return '契約登録'
    case 'UPDATE': return '契約更新'
    case 'RETURN': return '返却登録'
    default:       return op
  }
}


// ---- 日時フォーマット関数 ----

/**
 * ISO 8601 の日時文字列を日本語表記に変換する（年月日 + 時分）
 * @param v - ISO 8601 形式の日時文字列（または null）
 */
function formatDateTime(v: string | null): string {
  if (!v) return '—'
  return new Date(v).toLocaleString('ja-JP', {
    year: 'numeric', month: '2-digit', day: '2-digit',
    hour: '2-digit', minute: '2-digit',
  })
}

/**
 * YYYY-MM-DD 形式の日付文字列を日本語表記に変換する（年月日のみ）
 * @param v - YYYY-MM-DD 形式の日付文字列（または null）
 */
function formatDate(v: string | null): string {
  if (!v) return '—'
  return new Date(v + 'T00:00:00').toLocaleDateString('ja-JP')
}

// ---- 返却確認ダイアログ ----

/** 返却登録 確認ダイアログの表示フラグ */
const showReturnConfirm = ref(false)

/** 返却登録ボタン押下：確認ダイアログを開く */
function openReturnConfirm(): void {
  showReturnConfirm.value = true
}

/**
 * 返却ダイアログで「返却する」を選択：返却日とともに親に return イベントを通知する
 * @param returnDate - 入力された返却日（YYYY-MM-DD 形式）
 */
function onConfirmReturn(returnDate: string): void {
  emit('return', returnDate)
}

// ---- 更新フォーム送信 ----

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

.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* 横長2カラム構成のモーダル本体
   height: 90vh を明示することで flex 子要素が確定した高さを計算できるようにする。
   max-height だけだと子要素が「高さは自由」と判断し overflow-y: auto が機能しない。 */
.modal {
  background: white;
  border-radius: 12px;
  padding: 0;
  width: 900px;
  max-width: 95vw;
  height: 90vh;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* ヘッダー（全幅・固定） */
.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px 16px;
  border-bottom: 1px solid #e5e7eb;
  flex-shrink: 0;
}

.modal-title {
  font-size: 18px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0;
}

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

/* ボディ（左右カラム）
   min-height: 0 を付与しないと flex 子要素がコンテンツ高さまで伸び続け
   max-height による制限が効かなくなるため必須 */
.modal-body {
  display: flex;
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* ==============================
   左カラム：詳細・フォーム
   ============================== */

.detail-col {
  flex: 0 0 520px;
  min-height: 0;  /* スクロールを有効にするため必須 */
  padding: 20px 24px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

/* 縦区切り線 */
.col-divider {
  width: 1px;
  background: #e5e7eb;
  flex-shrink: 0;
}

/* 読み取り専用情報グリッド */
.info-grid {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

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

.info-label {
  color: #6b7280;
  font-weight: 500;
}

.divider {
  border-top: 1px solid #e5e7eb;
  margin: 4px 0 16px;
}

/* 編集フォーム */
.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
  margin-bottom: 4px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.form-group.full { grid-column: 1 / -1; }

.form-group label {
  font-size: 12px;
  font-weight: 600;
  color: #374151;
}

.required { color: #ef4444; }

/* タイトルとバッジを横並びにする行 */
.modal-title-row {
  display: flex;
  align-items: center;
  gap: 10px;
}

/* 返却済み・編集不可バッジ（タイトル横に表示） */
.readonly-badge {
  display: inline-block;
  font-size: 13px;
  font-weight: 600;
  color: #92400e;
  background: #fef3c7;
  border: 1px solid #fcd34d;
  border-radius: 6px;
  padding: 4px 12px;
  white-space: nowrap;
}

/* disabled 状態のフィールドを視覚的に区別する */
.input:disabled,
.select:disabled {
  background: #f3f4f6;
  color: #9ca3af;
  cursor: not-allowed;
  border-color: #e5e7eb;
}

.input, .select {
  height: 36px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  padding: 0 10px;
  font-size: 13px;
  outline: none;
}
.input:focus, .select:focus { border-color: #6366f1; }

.form-error {
  color: #ef4444;
  font-size: 13px;
  margin-top: 8px;
}

/* アクションボタン行（左カラム下部に固定） */
.modal-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: auto;
  padding-top: 16px;
  gap: 10px;
}

.right-actions {
  display: flex;
  gap: 10px;
  margin-left: auto;
}

/* ==============================
   右カラム：履歴パネル
   ============================== */

.history-col {
  flex: 1;
  min-height: 0;  /* スクロールを有効にするため必須 */
  padding: 20px 20px;
  overflow-y: auto;
  background: #fafafa;
}

/* 履歴パネルのタイトル */
.history-panel-title {
  font-size: 13px;
  font-weight: 700;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0 0 16px;
}

/* 各履歴セクション */
.history-section {
  margin-bottom: 24px;
}

.history-section:last-child {
  margin-bottom: 0;
}

.history-section-title {
  font-size: 13px;
  font-weight: 600;
  color: #374151;
  margin: 0 0 10px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e5e7eb;
}

/* タイムライン */
.timeline {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.timeline-item {
  display: flex;
  align-items: flex-start;
  gap: 10px;
}

.timeline-item--empty .timeline-label {
  color: #9ca3af;
}

/* タイムラインのドット（種別ごとに色分け） */
.timeline-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 3px;
}
.dot-create { background: #6366f1; }  /* 登録: インジゴ */
.dot-update { background: #f59e0b; }  /* 更新: オレンジ */
.dot-return { background: #10b981; }  /* 返却: グリーン */
.dot-fee    { background: #3b82f6; }  /* 月額: ブルー */
.dot-none   { background: #d1d5db; }  /* なし: グレー */

.timeline-content {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.timeline-label {
  font-size: 12px;
  font-weight: 600;
  color: #374151;
}

.timeline-date {
  font-size: 11px;
  color: #6b7280;
}

/* 月額は少し大きめで表示 */
.fee-value {
  font-size: 13px;
  font-weight: 600;
  color: #1a1a2e;
}

/* 読み込み中 / 履歴なし */
.history-loading { font-size: 13px; color: #9ca3af; padding: 8px 0; }
.history-empty   { font-size: 13px; color: #9ca3af; padding: 8px 0; }

/* ---- 履歴タイムライン ---- */
.history-timeline {
  list-style: none;
  margin: 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
}

/* 1グループ = 1操作 */
.history-group {
  border-left: 3px solid #e5e7eb;
  padding-left: 10px;
}

/* グループヘッダー（操作バッジ・日時・操作者） */
.history-group-header {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  margin-bottom: 4px;
}

/* 操作種別バッジ */
.history-op-badge {
  display: inline-block;
  padding: 1px 7px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
}
.op-create  { background: #ede9fe; color: #6d28d9; }  /* 契約登録: 紫 */
.op-update  { background: #fef3c7; color: #92400e; }  /* 契約更新: オレンジ */
.op-return  { background: #d1fae5; color: #065f46; }  /* 返却登録: 緑 */

/* 日時・操作者 */
.history-date { font-size: 11px; color: #6b7280; }
.history-user { font-size: 11px; color: #9ca3af; }

/* フィールド差分リスト */
.history-changes {
  list-style: none;
  margin: 4px 0 0;
  padding: 0;
  display: flex;
  flex-direction: column;
  gap: 3px;
}

.history-change {
  display: flex;
  align-items: baseline;
  gap: 6px;
  font-size: 12px;
  flex-wrap: wrap;
}

/* フィールドラベル */
.change-label {
  color: #6b7280;
  font-weight: 500;
  min-width: 68px;
  flex-shrink: 0;
}

/* 変更前後の値 */
.change-values { display: flex; align-items: center; gap: 4px; }
.change-old    { color: #9ca3af; text-decoration: line-through; }
.change-arrow  { color: #d1d5db; font-size: 10px; }
.change-new    { color: #1a1a2e; font-weight: 600; }

/* ==============================
   共通ボタンスタイル
   ============================== */

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
.btn-primary   { background: #6366f1; color: white; }
.btn-primary:hover:not(:disabled) { background: #4f46e5; }
.btn-ghost { background: transparent; color: #6b7280; border: 1px solid #e5e7eb; }
.btn-ghost:hover { background: #f9fafb; }
.btn-warning { background: #f59e0b; color: white; }
.btn-warning:hover { background: #d97706; }

/* ==============================
   テキストユーティリティ
   ============================== */
.mono        { font-family: monospace; }
.small-text  { font-size: 12px; }
.text-muted  { color: #9ca3af; }
.text-danger { color: #ef4444; }
.text-warning { color: #f59e0b; }
.font-bold   { font-weight: 700; }
</style>
