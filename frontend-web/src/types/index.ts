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
// 貸出管理
// =====================

export interface PcLoan {
  id: number
  pcAssetId: number
  assetNumber: string
  deviceName: string
  employeeId: number
  employeeName: string
  employeeCode: string
  loanDate: string
  expectedReturnDate: string | null
  actualReturnDate: string | null
  purpose: string | null
  note: string | null
  createdBy: number
  createdByName: string | null
  createdAt: string
  returned: boolean
  overdue: boolean
}

export interface LoanCreateRequest {
  pcAssetId: number
  employeeId: number
  loanDate: string
  expectedReturnDate?: string
  purpose?: string
  note?: string
}

export interface LoanReturnRequest {
  actualReturnDate: string
  note?: string
}

export interface LoanSearchParams {
  page?: number
  size?: number
  keyword?: string
  returned?: boolean
}

// =====================
// レンタル管理
// =====================

export interface PcRental {
  id: number
  pcAssetId: number
  assetNumber: string
  deviceName: string
  rentalVendorId: number
  vendorName: string
  contractNumber: string | null
  rentalStartDate: string
  rentalEndDate: string
  monthlyFee: number | null
  contractFilePath: string | null
  returnDate: string | null
  createdAt: string
  updatedAt: string
  returned: boolean
  expired: boolean
  daysUntilExpiry: number | null
}

export interface RentalVendor {
  id: number
  companyName: string
  contactName: string | null
  phone: string | null
  email: string | null
  address: string | null
  note: string | null
  createdAt: string
  updatedAt: string
}

export interface RentalCreateRequest {
  pcAssetId: number
  rentalVendorId: number
  contractNumber?: string
  rentalStartDate: string
  rentalEndDate: string
  monthlyFee?: number
  contractFilePath?: string
}

export interface RentalVendorCreateRequest {
  companyName: string
  contactName?: string
  phone?: string
  email?: string
  address?: string
  note?: string
}

export interface RentalSearchParams {
  page?: number
  size?: number
  keyword?: string
  returned?: boolean
  expiryFilter?: 'near' | 'expired'
}

// =====================
// ソフトウェアライセンス
// =====================

export interface SoftwareLicense {
  id: number
  softwareName: string
  publisher: string | null
  licenseType: string | null
  purchasedCount: number
  installedCount: number
  note: string | null
  createdAt: string
  updatedAt: string
  overLimit: boolean
}

export interface SoftwareCreateRequest {
  softwareName: string
  publisher?: string
  licenseType?: string
  purchasedCount?: number
  note?: string
}

export interface SoftwareSearchParams {
  page?: number
  size?: number
  keyword?: string
  overLimit?: boolean
}

// =====================
// ユーザー管理
// =====================

export interface SystemUser {
  id: number
  username: string
  displayName: string
  role: 'ADMIN' | 'IT_STAFF' | 'VIEWER'
  email: string | null
  isActive: boolean
  lastLoginAt: string | null
  createdAt: string
  updatedAt: string | null
}

export interface UserCreateRequest {
  username: string
  password: string
  displayName: string
  role: 'ADMIN' | 'IT_STAFF' | 'VIEWER'
  email?: string
}

export interface UserUpdateRequest {
  displayName: string
  role: 'ADMIN' | 'IT_STAFF' | 'VIEWER'
  email?: string
  isActive?: boolean
  password?: string
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
