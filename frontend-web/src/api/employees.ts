import apiClient from './axios'
import type {
  ApiResponse,
  PageResponse,
  Employee,
  EmployeeSearchParams,
  EmployeeCreateRequest,
  EmployeeUpdateRequest,
} from '@/types'

export const employeesApi = {
  /**
   * 社員一覧取得
   */
  findAll(params: EmployeeSearchParams = {}): Promise<ApiResponse<PageResponse<Employee>>> {
    return apiClient.get<ApiResponse<PageResponse<Employee>>>('/employees', { params })
      .then(res => res.data)
  },

  /**
   * 在籍社員リスト（プルダウン用）
   */
  findActiveList(): Promise<ApiResponse<Employee[]>> {
    return apiClient.get<ApiResponse<Employee[]>>('/employees/active')
      .then(res => res.data)
  },

  /**
   * 社員詳細取得
   */
  findById(id: number): Promise<ApiResponse<Employee>> {
    return apiClient.get<ApiResponse<Employee>>(`/employees/${id}`)
      .then(res => res.data)
  },

  /**
   * 社員登録
   */
  create(req: EmployeeCreateRequest): Promise<ApiResponse<Employee>> {
    return apiClient.post<ApiResponse<Employee>>('/employees', req)
      .then(res => res.data)
  },

  /**
   * 社員更新
   */
  update(id: number, req: EmployeeUpdateRequest): Promise<ApiResponse<Employee>> {
    return apiClient.put<ApiResponse<Employee>>(`/employees/${id}`, req)
      .then(res => res.data)
  },
}
