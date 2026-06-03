<!--
  機能ID: WEB_AUT0101
  views/WEB_AUT0101_LoginView.vue
  -----------------------------------------------
  ログイン画面コンポーネント

  - ユーザー名・パスワードを入力して認証する
  - 認証成功後は redirect クエリパラメータ（または /dashboard）へ遷移
  - ローディング中はフォームを無効化して二重送信を防止
  - エラー時はバックエンドのメッセージをフォーム上部に表示
  -----------------------------------------------
-->
<template>
  <!-- ページ全体を画面いっぱいに表示するラッパー -->
  <div class="login-page">
    <!-- 中央に配置するカード型フォーム -->
    <div class="login-card">

      <!-- ロゴ・タイトル領域 -->
      <div class="login-header">
        <!-- imageフォルダのアイコン画像を読み込む（規約: アイコンは画像化してimageフォルダに格納） -->
        <img src="@/image/icon-pc.svg" class="logo" alt="PC管理システム" />
        <h1 class="title">PC管理システム</h1>
        <p class="subtitle">ログインしてください</p>
      </div>

      <!--
        ログインフォーム
        @submit.prevent: フォームのデフォルト送信（ページリロード）を抑止し
        handleLogin を呼び出す
      -->
      <form class="login-form" @submit.prevent="handleLogin">

        <!-- エラーメッセージ（認証失敗時のみ表示） -->
        <div v-if="errorMessage" class="error-alert">
          {{ errorMessage }}
        </div>

        <!-- ユーザー名入力 -->
        <div class="form-group">
          <label for="username">ユーザー名</label>
          <input
            id="username"
            v-model="form.username"
            type="text"
            placeholder="ユーザー名を入力"
            autocomplete="username"
            :disabled="authStore.isLoading"
            required
          />
        </div>

        <!-- パスワード入力 -->
        <div class="form-group">
          <label for="password">パスワード</label>
          <input
            id="password"
            v-model="form.password"
            type="password"
            placeholder="パスワードを入力"
            autocomplete="current-password"
            :disabled="authStore.isLoading"
            required
          />
        </div>

        <!--
          ログインボタン
          isLoading が true の間は disabled にして二重送信を防止
        -->
        <button
          type="submit"
          class="login-button"
          :disabled="authStore.isLoading"
        >
          <!-- ローディング中とそれ以外でラベルを切り替え -->
          <span v-if="authStore.isLoading">ログイン中...</span>
          <span v-else>ログイン</span>
        </button>

      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * ログイン画面のロジック
 *
 * フォームの状態管理・送信処理・リダイレクト制御を担う。
 * 認証状態の更新は useAuthStore に委譲する。
 */
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

// Vue Router インスタンス（画面遷移に使用）
const router = useRouter()
// 現在のルート情報（redirect クエリパラメータの取得に使用）
const route  = useRoute()
// 認証ストア（ログイン処理・ローディング状態の参照）
const authStore = useAuthStore()

/**
 * フォーム入力値
 * username: ログインユーザー名
 * password: パスワード（平文、HTTPS 経由でのみ送信）
 */
const form = ref({ username: '', password: '' })

/** フォーム上部に表示するエラーメッセージ（'' = 非表示） */
const errorMessage = ref('')

/**
 * ログイン送信ハンドラ
 * 1. エラーメッセージをリセット
 * 2. authStore.login() で認証 API を呼び出す
 * 3. 成功時: redirect クエリパラメータ（未指定時は /dashboard）へ遷移
 * 4. 失敗時: バックエンドのエラーメッセージ（または汎用メッセージ）を表示
 */
async function handleLogin() {
  errorMessage.value = '' // 前回のエラーをクリア
  try {
    await authStore.login(form.value)
    // ログイン前にアクセスしようとしていたページへ戻す（未指定時はダッシュボード）
    const redirect = (route.query.redirect as string) || '/dashboard'
    router.push(redirect)
  } catch (err: any) {
    // バックエンドが返した詳細メッセージを優先表示し、なければ汎用メッセージ
    errorMessage.value =
      err.response?.data?.message || 'ログインに失敗しました。ユーザー名またはパスワードを確認してください。'
  }
}
</script>

<style scoped>
/* ==============================
   ページ全体レイアウト
   ============================== */

/* 画面全体を覆うグラデーション背景 */
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;      /* 垂直中央揃え */
  justify-content: center;  /* 水平中央揃え */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

/* ==============================
   カードコンテナ
   ============================== */

/* 白背景の丸みのある中央カード */
.login-card {
  background: #fff;
  border-radius: 12px;
  padding: 40px;
  width: 100%;
  max-width: 400px;   /* 最大幅制限（スマホでは全幅） */
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.15); /* 浮き上がりの影 */
}

/* ==============================
   ヘッダー（ロゴ・タイトル）
   ============================== */

/* ロゴ・タイトルを中央揃えで配置 */
.login-header {
  text-align: center;
  margin-bottom: 32px;
}

/* ロゴアイコン画像（imageフォルダの icon-pc.svg を使用） */
.logo {
  width: 48px;
  height: 48px;
  margin-bottom: 12px;
}

/* システム名タイトル */
.title {
  font-size: 22px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 8px;
}

/* サブタイトル（案内文） */
.subtitle {
  color: #6b7280;
  font-size: 14px;
  margin: 0;
}

/* ==============================
   フォーム
   ============================== */

/* フォーム全体を縦方向に並べ、要素間にギャップを設ける */
.login-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* エラーメッセージ（赤背景の警告ボックス） */
.error-alert {
  background: #fee2e2;
  border: 1px solid #fca5a5;
  color: #dc2626;
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
}

/* ラベル + 入力欄のグループ */
.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* フォームラベル */
.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #374151;
}

/* テキスト入力欄 */
.form-group input {
  padding: 12px 16px;
  border: 1px solid #d1d5db;
  border-radius: 8px;
  font-size: 15px;
  outline: none;
  transition: border-color 0.2s; /* フォーカス時のアニメーション */
}

/* フォーカス時: インジゴ色のボーダーとリングを表示 */
.form-group input:focus {
  border-color: #6366f1;
  box-shadow: 0 0 0 3px rgba(99, 102, 241, 0.15);
}

/* 無効状態: グレー背景でカーソルを変更 */
.form-group input:disabled {
  background: #f9fafb;
  cursor: not-allowed;
}

/* ==============================
   ログインボタン
   ============================== */

/* グラデーションボタン（ページ背景と統一感を持たせる） */
.login-button {
  padding: 14px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

/* ホバー時: 少し透過させて押せる感を演出 */
.login-button:hover:not(:disabled) {
  opacity: 0.9;
}

/* 無効状態（ローディング中）: 半透明でカーソルを変更 */
.login-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
</style>
