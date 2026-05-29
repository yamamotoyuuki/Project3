/**
 * api/axios.ts
 * -----------------------------------------------
 * Axios 共通クライアント設定
 *
 * - ベース URL・タイムアウト・Content-Type を統一設定
 * - リクエストインターセプタ: JWT トークンを Authorization ヘッダーに付与
 * - レスポンスインターセプタ: 401 エラー時に自動ログアウト & ログイン画面へリダイレクト
 *
 * 全 API モジュールはこのインスタンスを利用してリクエストを送信する。
 * -----------------------------------------------
 */
import axios from 'axios'
import { useAuthStore } from '@/stores/auth'

/**
 * Axios インスタンス（apiClient）
 * baseURL: Vite プロキシ設定により /api/v1 → http://localhost:8080/api/v1 に転送される。
 * timeout: 30 秒でタイムアウト（ネットワーク障害時のハング防止）。
 */
const apiClient = axios.create({
  baseURL: '/api/v1',          // バックエンド API のベースパス
  timeout: 30000,              // リクエストタイムアウト: 30 秒
  headers: {
    'Content-Type': 'application/json', // JSON 形式でリクエストを送信
  },
})

// =====================================================
// リクエストインターセプタ: JWT トークン付与
// =====================================================
apiClient.interceptors.request.use(
  (config) => {
    // localStorage からログイン時に保存した JWT トークンを取得
    const token = localStorage.getItem('token')
    if (token) {
      // Bearer 認証スキームでトークンをセット
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  // リクエスト生成自体が失敗した場合はそのまま reject
  (error) => Promise.reject(error)
)

// =====================================================
// レスポンスインターセプタ: 認証エラー時の自動ログアウト
// =====================================================
apiClient.interceptors.response.use(
  // 正常レスポンス: そのまま通過させる
  (response) => response,
  (error) => {
    const status = error.response?.status
    if (status === 401) {
      // HTTP 401: トークン期限切れ / 不正なトークン
      // → ストアの状態と localStorage をクリアしてログイン画面へ強制リダイレクト
      const authStore = useAuthStore()
      authStore.logout()
      window.location.href = '/login'
    } else if (status === 403) {
      // HTTP 403: 権限不足 or Spring Security が未認証リクエストに返す場合
      // → ログイン画面ではない場合のみリダイレクト（ログイン済みユーザーの権限不足は除外）
      const token = localStorage.getItem('token')
      if (!token) {
        // トークン未保持の場合は未認証とみなしてログイン画面へリダイレクト
        window.location.href = '/login'
      }
      // トークンがある場合は 403 をそのまま呼び出し元に返す（権限不足エラーとして表示）
    }
    return Promise.reject(error)
  }
)

export default apiClient
