/**
 * router/index.ts
 * -----------------------------------------------
 * Vue Router 設定ファイル
 *
 * - HTML5 History モードを使用（URL にハッシュを含まない）
 * - 各ルートは遅延ロード（import()）で定義し、初期バンドルを軽量化する
 * - meta フィールド:
 *     requiresAuth  : true の場合、未ログインユーザーをログイン画面へリダイレクト
 *     requiresAdmin : true の場合、ADMIN 以外のユーザーをダッシュボードへリダイレクト
 *
 * ナビゲーションガード（beforeEach）で認証・権限チェックを一元管理する。
 * -----------------------------------------------
 */
import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  // HTML5 History API を使用（nginx 側で try_files 設定が必要）
  history: createWebHistory(),
  routes: [
    // =====================================================
    // 認証系ルート
    // =====================================================
    {
      path: '/login',
      name: 'Login',
      // ログイン画面: 未認証ユーザーがアクセス可能
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false }, // 認証不要
    },
    // ルートパス（/）はダッシュボードへリダイレクト
    { path: '/', redirect: '/dashboard' },

    // =====================================================
    // メインコンテンツ（要認証）
    // =====================================================
    {
      path: '/dashboard',
      name: 'Dashboard',
      // ダッシュボード: 統計サマリーや最近のアクティビティを表示
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/assets',
      name: 'AssetList',
      // PC 資産一覧: 資産の検索・フィルタリング・新規登録
      component: () => import('@/views/AssetListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/assets/:id',
      name: 'AssetDetail',
      // PC 資産詳細: 個別資産の詳細情報・編集・貸出履歴の確認
      component: () => import('@/views/AssetDetailView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/employees',
      name: 'EmployeeList',
      // 社員一覧: 社員情報の管理（追加・編集・無効化）
      component: () => import('@/views/EmployeeListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/loans',
      name: 'LoanList',
      // 貸出管理: PC の貸出・返却・延滞管理
      component: () => import('@/views/LoanListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/rentals',
      name: 'RentalList',
      // レンタル管理: レンタル契約の管理・期限アラート
      component: () => import('@/views/RentalListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/software',
      name: 'SoftwareList',
      // ソフトウェアライセンス管理: 購入数・インストール数の管理
      component: () => import('@/views/SoftwareListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/users',
      name: 'UserList',
      // システムユーザー管理: ADMIN のみアクセス可能
      component: () => import('@/views/UserListView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true }, // 管理者専用
    },

    // =====================================================
    // フォールバック（定義外のパスはダッシュボードへ）
    // =====================================================
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

// =====================================================
// ナビゲーションガード（遷移前の認証・権限チェック）
// =====================================================
router.beforeEach((to) => {
  const authStore = useAuthStore()

  // ① 認証必須ページに未ログインユーザーがアクセスした場合
  //    → ログイン画面へリダイレクト（redirect クエリでアクセス先 URL を引き渡す）
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }

  // ② ログイン済みユーザーがログイン画面にアクセスした場合
  //    → ダッシュボードへリダイレクト（再ログイン不要）
  if (to.name === 'Login' && authStore.isLoggedIn) {
    return { name: 'Dashboard' }
  }

  // ③ ADMIN 専用ページに非 ADMIN ユーザーがアクセスした場合
  //    → ダッシュボードへリダイレクト（権限不足）
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: 'Dashboard' }
  }

  // 上記以外: そのまま遷移を許可（return undefined）
})

export default router
