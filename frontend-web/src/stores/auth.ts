import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api/auth'
import type { CurrentUser, LoginRequest } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  // ---- State ----
  const token = ref<string | null>(localStorage.getItem('token'))
  const currentUser = ref<CurrentUser | null>(
    (() => {
      const saved = localStorage.getItem('currentUser')
      return saved ? JSON.parse(saved) : null
    })()
  )
  const isLoading = ref(false)

  // ---- Getters ----
  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => currentUser.value?.role === 'ADMIN')
  const isItStaff = computed(
    () => currentUser.value?.role === 'ADMIN' || currentUser.value?.role === 'IT_STAFF'
  )

  // ---- Actions ----
  async function login(request: LoginRequest): Promise<void> {
    isLoading.value = true
    try {
      const response = await authApi.login(request)
      const loginData = response.data

      token.value = loginData.token
      currentUser.value = {
        userId: loginData.userId,
        username: loginData.username,
        displayName: loginData.displayName,
        role: loginData.role,
      }

      // localStorage に保存
      localStorage.setItem('token', loginData.token)
      localStorage.setItem('currentUser', JSON.stringify(currentUser.value))
    } finally {
      isLoading.value = false
    }
  }

  function logout(): void {
    token.value = null
    currentUser.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('currentUser')
  }

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
