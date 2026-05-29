import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

/**
 * vite.config.ts - Tauri エージェントアプリの Vite 設定
 *
 * Tauri の内蔵サーバーと連携するため clearScreen: false を設定。
 * envPrefix で VITE_ と TAURI_ 両方の環境変数を読み込む。
 */
export default defineConfig({
  plugins: [vue()],

  // Tauri が出力を上書きしないよう画面クリアを無効化
  clearScreen: false,

  server: {
    // Tauri が固定ポートを期待するため strictPort を有効に
    strictPort: true,
    port: 5173,
    watch: {
      // Rust のビルド成果物（target/）を監視対象から除外
      // Windows で DLL がロック中に EBUSY エラーが発生するのを防ぐ
      ignored: ['**/src-tauri/target/**'],
    },
  },

  // Tauri が利用する環境変数プレフィックス
  envPrefix: ['VITE_', 'TAURI_'],

  build: {
    // Tauri はデスクトップ向けなのでモダンブラウザをターゲットにする
    target: ['es2021', 'chrome105', 'safari15'],
    // Vite のビルドエラーをターミナルに表示する
    minify: !process.env.TAURI_DEBUG ? 'esbuild' : false,
    sourcemap: !!process.env.TAURI_DEBUG,
  },
})
