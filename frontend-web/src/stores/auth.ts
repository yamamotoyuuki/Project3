/**
 * stores/auth.ts
 * -----------------------------------------------
 * 認証状態管理ストア（Pinia）
 *
 * JWT トークンとログインユーザー情報をアプリ全体で共有する。
 * ブラウザリロード後も認証状態を維持するため、localStorage に永続化する。
 *
 * 公開 State:
 *   token       - JWT アクセストークン（null = 未ログイン）
 *   currentUser - ログイン中ユーザーの基本情報
 *   isLoading   - ログイン API 呼び出し中フラグ
 *
 * 公開 Getters:
 *   isLoggedIn  - ログイン済みかどうか（token が存在するか）
 *   isAdmin     - ADMIN ロールかどうか
 *   isItStaff   - ADMIN または IT_STAFF ロールかどうか
 *
 * 公開 Actions:
 *   login()     - ログイン API を呼び出しトークン・ユーザー情報を保存
 *   logout()    - 状態と localStorage をクリアしてログアウト
 * -----------------------------------------------
 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { CurrentUser, LoginRequest } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  // =====================================================
  // State（リアクティブな状態変数）
  // =====================================================

  /**
   * JWT アクセストークン
   * 起動時に localStorage から復元する（ページリロード対応）。
   * null の場合は未ログイン状態。
   */
  const token = ref<string | null>(localStorage.getItem('token'))

  /**
   * ログイン中のユーザー情報（userId / username / displayName / role）
   * 起動時に localStorage から JSON パースして復元する。
   */
  const currentUser = ref<CurrentUser | null>(
    (() => {
      const saved = localStorage.getItem('currentUser')
      // 保存データが存在すればパース、なければ null を初期値とする
      return saved ? JSON.parse(saved) : null
    })()
  )

  /**
   * ログイン API の処理中フラグ
   * true の間はフォームボタンを disabled にして二重送信を防ぐ。
   */
  const isLoading = ref(false)

  // =====================================================
  // Getters（computed から派生する読み取り専用プロパティ）
  // =====================================================

  /** ログイン済みかどうか（token が null でなければ true） */
  const isLoggedIn = computed(() => !!token.value)

  /** ADMIN ロールかどうか（全機能へのアクセス権限を持つ） */
  const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')

  /**
   * IT スタッフ以上の権限かどうか
   * ADMIN または IT_STAFF ロールであれば true を返す。
   * VIEWER はこのフラグが false になる。
   */
  const isItStaff = computed(
    () => currentUser.value?.role === 'ADMIN' || currentUser.value?.role === 'IT_STAFF'
  )

  // =====================================================
  // Actions（状態を変更する関数）
  // =====================================================

  /**
   * ログイン処理
   * 1. API を呼び出して JWT トークンとユーザー情報を取得
   * 2. リアクティブな state を更新
   * 3. localStorage に永続化（ページリロード後も認証状態を維持）
   *
   * @param request - ユーザー名・パスワードを含むログインリクエスト
   * @throws AxiosError - 認証失敗またはサーバーエラー時（呼び出し元でハンドリング）
   */
  async function login(request: LoginRequest): Promise<void> {
    isLoading.value = true
    try {
      // バックエンドの認証 API を呼び出す
      const response = await authApi.login(request)
      const loginData = response.data

      // JWT トークンをストアに保存
      token.value = loginData.token

      // ユーザー情報をストアに保存（表示に必要な最低限の情報のみ）
      currentUser.value = {
        userId:      loginData.userId,
        username:    loginData.username,
        displayName: loginData.displayName,
        role:        loginData.role,
      }

      // ページリロード後も状態を維持するため localStorage に書き込む
      localStorage.setItem('token', loginData.token)
      localStorage.setItem('currentUser', JSON.stringify(currentUser.value))
    } finally {
      // 成功・失敗に関わらずローディングフラグを解除
      isLoading.value = false
    }
  }

  /**
   * ログアウト処理
   * ストアの state をリセットし、localStorage から認証情報を削除する。
   * ルーターへのリダイレクトは呼び出し元（インターセプタや UI）が担う。
   */
  function logout(): void {
    token.value = null          // トークンをクリア
    currentUser.value = null    // ユーザー情報をクリア
    localStorage.removeItem('token')       // 永続化されたトークンを削除
    localStorage.removeItem('currentUser') // 永続化されたユーザー情報を削除
  }

  // ストアが公開するプロパティ・関数を返す
  return {
    token,
    currentUser,
    isLoading,
    isLoggedIn,
    isAdmin,
    isItStaff,
    login,
    logout,
  }
})
