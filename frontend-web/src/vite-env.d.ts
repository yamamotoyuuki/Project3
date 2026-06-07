/// <reference types="vite/client" />

/**
 * Vite クライアント型定義ファイル
 *
 * - SVG ファイルの import に型を付与する（CannotFindModule エラー抑止）
 * - import.meta.env のプロジェクト固有変数を定義する
 * - VITE_ プレフィックスの変数のみクライアント側に公開される
 */

// =====================================================
// アセット型定義（SVG / PNG など画像ファイルの import）
// =====================================================

/**
 * SVG ファイルを文字列 URL として import する型定義
 * 例: import iconPc from '@/image/icon-pc.svg'  // string 型として解決される
 */
declare module '*.svg' {
  const src: string
  export default src
}

/** PNG ファイルの import 型定義 */
declare module '*.png' {
  const src: string
  export default src
}

/** JPEG ファイルの import 型定義 */
declare module '*.jpg' {
  const src: string
  export default src
}

// =====================================================
// import.meta.env 型定義
// =====================================================

interface ImportMetaEnv {
  /**
   * バックエンド API のベース URL（開発環境向けフォールバック用）
   * .env ファイルに VITE_API_BASE_URL=http://localhost:8080/api/v1 を設定する
   */
  readonly VITE_API_BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
