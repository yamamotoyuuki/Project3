import apiClient from './axios'
import type { ApiResponse, LoginRequest, LoginResponse } from '@/types'

export const authApi = {
  /**
   * ログイン
   */
  login(request: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    return apiClient.post<ApiResponse<LoginResponse>>('/auth/login', request)
      .then(res => res.data)
  },

  /**
   * ログアウト
   */
  logout(): Promise<void> {
    return apiClient.post('/auth/logout').then(() => {})
  },
}
