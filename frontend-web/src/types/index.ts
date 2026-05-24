// =====================
// 共通型定義
// =====================

export interface ApiResponse<T> {
  code: string
  message: string
  data: T
  timestamp: string
}

// =====================
// 認証
// =====================

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  expiresIn: number
  userId: number
  username: string
  displayName: string
  role: 'ADMIN' | 'IT_STAFF' | 'VIEWER'
}

export interface CurrentUser {
  userId: number
  username: string
  displayName: string
  role: 'ADMIN' | 'IT_STAFF' | 'VIEWER'
}

// =====================
// PC資産
// =====================

export type AcquisitionType = 'PURCHASE' | 'RENTAL'
export type PcStatus = 'IN_USE' | 'IN_STORAGE' | 'DISPOSED' | 'IN_REPAIR' | 'RETURNED'

export interface PcAsset {
  id: number
  assetNumber: string
  deviceName: string
  acquisitionType: AcquisitionType
  maker: string | null
  modelNumber: string | null
  serialNumber: string | null
  location: string | null
  status: PcStatus
  assignedEmployeeId: number | null
  hostname: string | null
  agentLastSeen: string | null
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface PcAssetListParams {
  page?: number
  size?: number
  status?: PcStatus
  acquisitionType?: AcquisitionType
  keyword?: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
}
