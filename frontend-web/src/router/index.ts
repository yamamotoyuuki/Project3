import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
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
    {
      path: '/dashboard',
      name: 'Dashboard',
      component: () => import('@/views/DashboardView.vue'),
      meta: { requiresAuth: true },
    },
    // Phase 2 以降で追加予定
    // { path: '/assets', name: 'AssetList', component: ..., meta: { requiresAuth: true } },
    // { path: '/assets/:id', name: 'AssetDetail', component: ..., meta: { requiresAuth: true } },
    // { path: '/rentals', name: 'RentalList', component: ..., meta: { requiresAuth: true } },
    // { path: '/loans', name: 'LoanList', component: ..., meta: { requiresAuth: true } },
    // { path: '/software', name: 'SoftwareList', component: ..., meta: { requiresAuth: true } },
    // { path: '/employees', name: 'EmployeeList', component: ..., meta: { requiresAuth: true } },
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
