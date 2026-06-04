import apiClient from './axios'
import type {
  ApiResponse, PageResponse,
  PcRental, RentalCreateRequest, RentalUpdateRequest, RentalReturnRequest, RentalSearchParams,
  RentalVendor, RentalVendorCreateRequest, RentalHistoryEntry,
} from '@/types'

export const rentalsApi = {
  // ---- 契約 ----
  findAll(params: RentalSearchParams = {}): Promise<ApiResponse<PageResponse<PcRental>>> {
    return apiClient.get<ApiResponse<PageResponse<PcRental>>>('/rentals', { params }).then(r => r.data)
  },
  findById(id: number): Promise<ApiResponse<PcRental>> {
    return apiClient.get<ApiResponse<PcRental>>(`/rentals/${id}`).then(r => r.data)
  },
  create(req: RentalCreateRequest): Promise<ApiResponse<PcRental>> {
    return apiClient.post<ApiResponse<PcRental>>('/rentals', req).then(r => r.data)
  },
  update(id: number, req: RentalUpdateRequest): Promise<ApiResponse<PcRental>> {
    return apiClient.put<ApiResponse<PcRental>>(`/rentals/${id}`, req).then(r => r.data)
  },
  /** 返却日を指定してレンタル品の返却を登録する */
  returnRental(id: number, req: RentalReturnRequest): Promise<ApiResponse<PcRental>> {
    return apiClient.put<ApiResponse<PcRental>>(`/rentals/${id}/return`, req).then(r => r.data)
  },

  /** 指定レンタル契約の変更履歴一覧を取得する */
  getHistories(id: number): Promise<ApiResponse<RentalHistoryEntry[]>> {
    return apiClient.get<ApiResponse<RentalHistoryEntry[]>>(`/rentals/${id}/histories`).then(r => r.data)
  },

  // ---- ベンダー ----
  findAllVendors(): Promise<ApiResponse<RentalVendor[]>> {
    return apiClient.get<ApiResponse<RentalVendor[]>>('/rental-vendors').then(r => r.data)
  },
  createVendor(req: RentalVendorCreateRequest): Promise<ApiResponse<RentalVendor>> {
    return apiClient.post<ApiResponse<RentalVendor>>('/rental-vendors', req).then(r => r.data)
  },
  updateVendor(id: number, req: RentalVendorCreateRequest): Promise<ApiResponse<RentalVendor>> {
    return apiClient.put<ApiResponse<RentalVendor>>(`/rental-vendors/${id}`, req).then(r => r.data)
  },
}
