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
    { path: '/', redirect: '/dashboard' },

    // ---- メイン ----
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
    {
      path: '/loans',
      name: 'LoanList',
      component: () => import('@/views/LoanListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/rentals',
      name: 'RentalList',
      component: () => import('@/views/RentalListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/software',
      name: 'SoftwareList',
      component: () => import('@/views/SoftwareListView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/users',
      name: 'UserList',
      component: () => import('@/views/UserListView.vue'),
      meta: { requiresAuth: true, requiresAdmin: true },
    },

    // フォールバック
    { path: '/:pathMatch(.*)*', redirect: '/dashboard' },
  ],
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.meta.requiresAuth && !authStore.isLoggedIn) {
    return { name: 'Login', query: { redirect: to.fullPath } }
  }
  if (to.name === 'Login' && authStore.isLoggedIn) {
    return { name: 'Dashboard' }
  }
  if (to.meta.requiresAdmin && !authStore.isAdmin) {
    return { name: 'Dashboard' }
  }
})

export default router
