/**
 * vite.config.ts
 * -----------------------------------------------
 * Vite ビルドツール設定ファイル
 *
 * - Vue 3 プラグインの有効化
 * - パスエイリアス "@" → "src/" の設定（import 文を簡潔に保つ）
 * - 開発サーバー設定:
 *     ポート 3000 で起動
 *     /api/* への API リクエストをバックエンド（8080）へプロキシ転送
 * -----------------------------------------------
 */
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [
    // Vue 3 の Single File Component（.vue）を処理するプラグイン
    vue(),
  ],
  resolve: {
    alias: {
      // "@/..." を "src/..." に解決するエイリアス
      // 例: import '@/stores/auth' → import './src/stores/auth'
      '@': resolve(__dirname, 'src'),
    },
  },
  server: {
    // 開発サーバーのポート番号
    port: 3000,
    proxy: {
      /**
       * /api/* へのリクエストをバックエンドサーバーに転送するプロキシ設定
       * これにより開発中に CORS エラーを回避できる。
       * 本番環境では nginx の proxy_pass 設定が同様の役割を担う。
       */
      '/api': {
        target: 'http://localhost:8080', // Spring Boot バックエンドの URL
        changeOrigin: true,              // Host ヘッダーをターゲットに合わせて書き換え
      },
    },
  },
})
