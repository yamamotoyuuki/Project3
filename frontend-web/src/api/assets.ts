import apiClient from './axios'
import type {
  ApiResponse,
  PageResponse,
  PcAsset,
  AssetSearchParams,
  AssetCreateRequest,
  AssetUpdateRequest,
} from '@/types'

export const assetsApi = {
  /**
   * PC資産一覧取得
   */
  findAll(params: AssetSearchParams = {}): Promise<ApiResponse<PageResponse<PcAsset>>> {
    return apiClient.get<ApiResponse<PageResponse<PcAsset>>>('/assets', { params })
      .then(res => res.data)
  },

  /**
   * PC資産詳細取得
   */
  findById(id: number): Promise<ApiResponse<PcAsset>> {
    return apiClient.get<ApiResponse<PcAsset>>(`/assets/${id}`)
      .then(res => res.data)
  },

  /**
   * PC資産登録
   */
  create(req: AssetCreateRequest): Promise<ApiResponse<PcAsset>> {
    return apiClient.post<ApiResponse<PcAsset>>('/assets', req)
      .then(res => res.data)
  },

  /**
   * PC資産更新
   */
  update(id: number, req: AssetUpdateRequest): Promise<ApiResponse<PcAsset>> {
    return apiClient.put<ApiResponse<PcAsset>>(`/assets/${id}`, req)
      .then(res => res.data)
  },

  /**
   * PC資産削除
   */
  delete(id: number): Promise<ApiResponse<void>> {
    return apiClient.delete<ApiResponse<void>>(`/assets/${id}`)
      .then(res => res.data)
  },
}
