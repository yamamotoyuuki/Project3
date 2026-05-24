import apiClient from './axios'
import type { ApiResponse, SystemUser, UserCreateRequest, UserUpdateRequest } from '@/types'

export const usersApi = {
  findAll(): Promise<ApiResponse<SystemUser[]>> {
    return apiClient.get<ApiResponse<SystemUser[]>>('/users').then(r => r.data)
  },
  findById(id: number): Promise<ApiResponse<SystemUser>> {
    return apiClient.get<ApiResponse<SystemUser>>(`/users/${id}`).then(r => r.data)
  },
  create(req: UserCreateRequest): Promise<ApiResponse<SystemUser>> {
    return apiClient.post<ApiResponse<SystemUser>>('/users', req).then(r => r.data)
  },
  update(id: number, req: UserUpdateRequest): Promise<ApiResponse<SystemUser>> {
    return apiClient.put<ApiResponse<SystemUser>>(`/users/${id}`, req).then(r => r.data)
  },
}
