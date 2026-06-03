import apiClient from './axios'
import type {
  ApiResponse,
  PageResponse,
  PcAsset,
  AssetSearchParams,
  AssetCreateRequest,
  AssetUpdateRequest,
  InstalledSoftware,
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

  /**
   * インストール済みソフトウェア一覧取得
   * PC資産の agent_number に紐付くソフトウェアをソフトウェア名昇順で返す。
   * エージェント未導入の場合は空配列。
   */
  getSoftware(id: number): Promise<ApiResponse<InstalledSoftware[]>> {
    return apiClient.get<ApiResponse<InstalledSoftware[]>>(`/assets/${id}/software`)
      .then(res => res.data)
  },
}
