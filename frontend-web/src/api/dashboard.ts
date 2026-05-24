import apiClient from './axios'
import type { ApiResponse, DashboardStats } from '@/types'

export const dashboardApi = {
  getStats(): Promise<ApiResponse<DashboardStats>> {
    return apiClient.get<ApiResponse<DashboardStats>>('/dashboard/stats')
      .then(res => res.data)
  },
}
