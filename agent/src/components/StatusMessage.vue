<!--
  agent/src/components/StatusMessage.vue
  -----------------------------------------------
  ステータスメッセージ共通コンポーネント

  各種メッセージ（成功・エラー・警告・処理中）の表示を共通化する。
  フェードアウトの可否を enableFadeOut プロパティで制御できる。

  使用例（フェードアウトあり）:
    <StatusMessage
      :message="sendStatus.message"
      :messageType="sendStatus.type"
      :isFading="isSendStatusFading"
      :enableFadeOut="true"
    />

  使用例（フェードアウトなし・エラーは常に表示）:
    <StatusMessage
      variant="popup"
      :message="registerPopup.message"
      :messageType="registerPopup.type"
      :isFading="isRegisterPopupFading"
      :enableFadeOut="registerPopup.type === 'success'"
      @close="closeRegisterPopup"
    />

  表示バリアント:
    - toast  : 画面中央フロート表示（デフォルト）
    - popup  : 画面中央オーバーレイポップアップ（登録結果など）
-->
<template>
  <!-- バリアント: toast - 画面中央フロート表示（影付き・pointer-events なし） -->
  <div v-if="variant === 'toast' && messageType" class="toast-overlay">
    <p class="status-msg" :class="[messageType, { fading: shouldFade }]">
      <!-- メッセージタイプに応じてimageフォルダのアイコン画像を読み込む（規約: アイコンは画像化） -->
      <img
        v-if="messageType === 'success' || messageType === 'ok'"
        src="../image/icon-success.svg"
        class="status-icon"
        alt="success"
      />
      <img
        v-else-if="messageType === 'error'"
        src="../image/icon-error.svg"
        class="status-icon"
        alt="error"
      />
      <img
        v-else-if="messageType === 'warning' || messageType === 'warn'"
        src="../image/icon-warning.svg"
        class="status-icon"
        alt="warning"
      />
      {{ message }}
    </p>
  </div>

  <!-- バリアント: popup - 画面中央オーバーレイポップアップ（登録結果など） -->
  <div
    v-else-if="variant === 'popup' && messageType"
    class="register-popup-overlay"
    @click.self="emit('close')"
  >
    <div class="register-popup-content" :class="[messageType, { fading: shouldFade }]">
      <!-- メッセージタイプに応じてimageフォルダのアイコン画像を読み込む -->
      <img
        v-if="messageType === 'success'"
        src="../image/icon-success.svg"
        class="register-popup-icon"
        alt="success"
      />
      <img
        v-else-if="messageType === 'error'"
        src="../image/icon-error.svg"
        class="register-popup-icon"
        alt="error"
      />
      <!-- メッセージ本文（複数行対応） -->
      <p class="register-popup-message">{{ message }}</p>
      <!-- 閉じるボタン（成功・失敗ともに表示） -->
      <button class="register-popup-close" @click="emit('close')">閉じる</button>
    </div>
  </div>
</template>

<script setup lang="ts">
  import { computed } from 'vue'

  // -------------------------------------------------------
  // 型定義
  // -------------------------------------------------------

  /** 表示バリアント型 */
  type Variant = 'toast' | 'popup'

  /** メッセージタイプ型 */
  type MessageType = 'success' | 'error' | 'loading' | 'warning' | 'ok' | 'warn' | ''

  // -------------------------------------------------------
  // プロパティ定義
  // -------------------------------------------------------

  const props = withDefaults(
    defineProps<{
      /** 表示するメッセージ文字列 */
      message: string
      /** メッセージタイプ（アイコン・背景色・文字色の決定に使用） */
      messageType: MessageType
      /** 現在フェードアウト中かどうか（タイマー管理は呼び出し元が行う） */
      isFading: boolean
      /** フェードアウトを有効にするかどうか（true: フェードアウトする / false: しない） */
      enableFadeOut: boolean
      /** 表示バリアント（省略時は 'toast'） */
      variant?: Variant
    }>(),
    {
      variant: 'toast',
    }
  )

  // -------------------------------------------------------
  // イベント定義
  // -------------------------------------------------------

  /** popup バリアントの閉じるイベント（「閉じる」ボタンまたはオーバーレイクリック時に発火） */
  const emit = defineEmits<{
    (e: 'close'): void
  }>()

  // -------------------------------------------------------
  // 算出プロパティ
  // -------------------------------------------------------

  /**
   * フェードアウト CSS クラスを適用するかどうか
   * isFading が true かつ enableFadeOut が true の場合にのみ fading クラスを付与する
   */
  const shouldFade = computed(() => props.isFading && props.enableFadeOut)
</script>

<style scoped>
  /* ==============================
   toast バリアント: 画面中央フロート表示
   ============================== */

  /* 画面中央に固定配置するオーバーレイ（クリック透過） */
  .toast-overlay {
    position: fixed;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    z-index: 200;
    pointer-events: none;
  }

  /* メッセージ本体（アイコン + テキストを横並び） */
  .status-msg {
    padding: 10px 28px;
    border-radius: 8px;
    margin: 0;
    font-size: 14px;
    font-weight: 600;
    white-space: nowrap;
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.18);
    /* フェードアウトアニメーション用トランジション（fading クラス付与時に opacity: 0 へ遷移） */
    opacity: 1;
    transition: opacity 0.5s ease;
    display: flex;
    align-items: center;
    gap: 6px;
  }

  /* フェードアウト中（JS が付与） */
  .status-msg.fading {
    opacity: 0;
  }

  /* 緑: 成功（success / ok 共通） */
  .status-msg.success,
  .status-msg.ok {
    background: #d1fae5;
    color: #065f46;
  }

  /* 赤: エラー */
  .status-msg.error {
    background: #fee2e2;
    color: #dc2626;
  }

  /* 紫: 処理中 */
  .status-msg.loading {
    background: #ede9fe;
    color: #5b21b6;
  }

  /* オレンジ: 警告（warning / warn 共通） */
  .status-msg.warning,
  .status-msg.warn {
    background: #fff7ed;
    color: #92400e;
    /* 改行文字（\n）を改行として描画する */
    white-space: pre-line;
  }

  /* ステータスメッセージ内のアイコン画像 */
  .status-icon {
    width: 16px;
    height: 16px;
    flex-shrink: 0;
  }

  /* ==============================
   popup バリアント: 登録結果ポップアップ
   ============================== */

  /* 半透明オーバーレイ（クリックで閉じる） */
  .register-popup-overlay {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 150;
    padding: 20px;
  }

  /* ポップアップ本体（白カード） */
  .register-popup-content {
    background: white;
    border-radius: 12px;
    padding: 28px 24px 20px;
    width: 100%;
    max-width: 300px;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 14px;
    box-shadow: 0 10px 30px rgba(0, 0, 0, 0.3);
    /* フェードアウトアニメーション用トランジション */
    opacity: 1;
    transition: opacity 0.5s ease;
  }

  /* フェードアウト中（JS が付与） */
  .register-popup-content.fading {
    opacity: 0;
  }

  /* 成功時: 上部に緑のアクセントボーダー */
  .register-popup-content.success {
    border-top: 4px solid #10b981;
  }

  /* 失敗時: 上部に赤のアクセントボーダー */
  .register-popup-content.error {
    border-top: 4px solid #dc2626;
  }

  /* 結果アイコン画像 */
  .register-popup-icon {
    width: 40px;
    height: 40px;
  }

  /* メッセージ本文（複数行対応） */
  .register-popup-message {
    margin: 0;
    font-size: 14px;
    text-align: center;
    color: #374151;
    line-height: 1.7;
    /* 改行文字（\n）を改行として描画する */
    white-space: pre-line;
  }

  /* 閉じるボタン */
  .register-popup-close {
    background: #f3f4f6;
    color: #374151;
    border: 1px solid #d1d5db;
    padding: 9px 0;
    border-radius: 8px;
    font-size: 14px;
    font-weight: 600;
    cursor: pointer;
    width: 100%;
  }

  .register-popup-close:hover {
    background: #e5e7eb;
  }
</style>
