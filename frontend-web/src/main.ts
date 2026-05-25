/**
 * main.ts
 * -----------------------------------------------
 * アプリケーションエントリーポイント
 *
 * - Vue アプリインスタンスの生成
 * - Pinia（グローバル状態管理）の登録
 * - Vue Router（SPA ルーティング）の登録
 * - グローバルスタイルの読み込み
 * - #app 要素へのマウント
 * -----------------------------------------------
 */
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import './assets/main.css' // グローバルスタイル（リセット・共通変数など）

// ルートコンポーネント（App.vue）を基にアプリインスタンスを生成
const app = createApp(App)

// Pinia を登録（useXxxStore() で各コンポーネントから利用可能になる）
app.use(createPinia())

// Vue Router を登録（<RouterView> / <RouterLink> が有効になる）
app.use(router)

// index.html の <div id="app"> にアプリをマウントして描画開始
app.mount('#app')
