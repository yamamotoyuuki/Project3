import apiClient from './axios'
import type { ApiResponse, PageResponse, PcLoan, LoanCreateRequest, LoanReturnRequest, LoanSearchParams } from '@/types'

export const loansApi = {
  findAll(params: LoanSearchParams = {}): Promise<ApiResponse<PageResponse<PcLoan>>> {
    return apiClient.get<ApiResponse<PageResponse<PcLoan>>>('/loans', { params }).then(r => r.data)
  },
  findById(id: number): Promise<ApiResponse<PcLoan>> {
    return apiClient.get<ApiResponse<PcLoan>>(`/loans/${id}`).then(r => r.data)
  },
  create(req: LoanCreateRequest): Promise<ApiResponse<PcLoan>> {
    return apiClient.post<ApiResponse<PcLoan>>('/loans', req).then(r => r.data)
  },
  returnLoan(id: number, req: LoanReturnRequest): Promise<ApiResponse<PcLoan>> {
    return apiClient.put<ApiResponse<PcLoan>>(`/loans/${id}/return`, req).then(r => r.data)
  },
}
