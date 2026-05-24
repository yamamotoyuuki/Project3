import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    // ---- 認証 ----
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      redirect: '/dashboard',
    },

    // ---- メイン（認証必須） ----
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/assets',
      name: 'AssetList',
      component: () => import('@/views/AssetListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/assets/:id',
      name: 'AssetDetail',
      component: () => import('@/views/AssetDetailView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/employees',
      name: 'EmployeeList',
      component: () => import('@/views/EmployeeListView.vue'),
      meta: { requiresAuth: true },
    },

    // Phase 3 以降で追加予定
    // { path: '/loans',    name: 'LoanList',    component: ..., meta: { requiresAuth: true } },
    // { path: '/rentals',  name: 'RentalList',  component: ..., meta: { requiresAuth: true } },
    // { path: '/software', name: 'SoftwareList', component: ..., meta: { requiresAuth: true } },
    // { path: '/users',    name: 'UserList',    component: ..., meta: { requiresAuth: true } },

    // フォールバック
    {
      path: '/:pathMatch(.*)*',
      redirect: '/dashboard',
    },
  ],
})

// ナビゲーションガード: 未認証ならログインへ
router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'Login' && authStore.isLoggedIn) {
    return { name: 'Dashboard' }
  }
})

export default router
