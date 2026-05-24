// =====================
// 共通型定義
// =====================

export interface ApiResponse<T> {
  code: string
  message: string
  data: T
  timestamp: string
}

export interface PageResponse<T> {
  content: T[]
  totalElements: number
  totalPages: number
  page: number
  size: number
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

export const PC_STATUS_LABELS: Record<PcStatus, string> = {
  IN_USE:     '使用中',
  IN_STORAGE: '保管中',
  DISPOSED:   '廃棄済',
  IN_REPAIR:  '修理中',
  RETURNED:   '返却済',
}

export const ACQUISITION_TYPE_LABELS: Record<AcquisitionType, string> = {
  PURCHASE: '購入',
  RENTAL:   'レンタル',
}

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
  assignedEmployeeName: string | null
  hostname: string | null
  agentLastSeen: string | null
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface AssetSearchParams {
  page?: number
  size?: number
  status?: PcStatus | ''
  acquisitionType?: AcquisitionType | ''
  keyword?: string
  location?: string
}

export interface AssetCreateRequest {
  assetNumber: string
  deviceName: string
  acquisitionType: AcquisitionType
  maker?: string
  modelNumber?: string
  serialNumber?: string
  location?: string
  status?: PcStatus
  assignedEmployeeId?: number | null
  hostname?: string
  note?: string
}

export interface AssetUpdateRequest {
  deviceName: string
  acquisitionType: AcquisitionType
  maker?: string
  modelNumber?: string
  serialNumber?: string
  location?: string
  status: PcStatus
  assignedEmployeeId?: number | null
  hostname?: string
  note?: string
}

// =====================
// 社員
// =====================

export interface Employee {
  id: number
  employeeCode: string
  fullName: string
  department: string | null
  position: string | null
  email: string | null
  phone: string | null
  location: string | null
  isActive: boolean
  createdAt: string
  updatedAt: string
}

export interface EmployeeSearchParams {
  page?: number
  size?: number
  keyword?: string
  isActive?: boolean | ''
}

export interface EmployeeCreateRequest {
  employeeCode: string
  fullName: string
  department?: string
  position?: string
  email?: string
  phone?: string
  location?: string
}

export interface EmployeeUpdateRequest {
  fullName: string
  department?: string
  position?: string
  email?: string
  phone?: string
  location?: string
  isActive?: boolean
}

// =====================
// ダッシュボード
// =====================

export interface DashboardStats {
  totalPcCount: number
  inUsePcCount: number
  inStoragePcCount: number
  activeLoansCount: number
  nearExpiryRentalsCount: number
  expiredRentalsCount: number
  licenseOverCount: number
}
