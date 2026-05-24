import apiClient from './axios'
import type { ApiResponse, PageResponse, SoftwareLicense, SoftwareCreateRequest, SoftwareSearchParams } from '@/types'

export const softwareApi = {
  findAll(params: SoftwareSearchParams = {}): Promise<ApiResponse<PageResponse<SoftwareLicense>>> {
    return apiClient.get<ApiResponse<PageResponse<SoftwareLicense>>>('/software', { params }).then(r => r.data)
  },
  findById(id: number): Promise<ApiResponse<SoftwareLicense>> {
    return apiClient.get<ApiResponse<SoftwareLicense>>(`/software/${id}`).then(r => r.data)
  },
  create(req: SoftwareCreateRequest): Promise<ApiResponse<SoftwareLicense>> {
    return apiClient.post<ApiResponse<SoftwareLicense>>('/software', req).then(r => r.data)
  },
  update(id: number, req: SoftwareCreateRequest): Promise<ApiResponse<SoftwareLicense>> {
    return apiClient.put<ApiResponse<SoftwareLicense>>(`/software/${id}`, req).then(r => r.data)
  },
}
