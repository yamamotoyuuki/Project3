/**
 * types/index.ts
 * -----------------------------------------------
 * アプリケーション全体で使用する型定義
 *
 * バックエンド API のレスポンス・リクエスト形式に対応した
 * TypeScript インターフェース・型エイリアス・定数を一元管理する。
 * -----------------------------------------------
 */

// =====================================================
// 共通型定義
// =====================================================

/**
 * コードマスタ値
 * GET /api/v1/common/codes/{codeType} のレスポンスアイテム型。
 * ドロップダウンリストの1選択肢（コード値と表示ラベルのペア）を表す。
 */
export interface CodeValue {
  codeValue: string  // コード値（他テーブルに格納される定数。例: "PURCHASE", "IN_USE"）
  codeLabel: string  // 画面に表示する日本語ラベル（例: "購入", "使用中"）
}

/**
 * 汎用 API レスポンスラッパー
 * バックエンドの全エンドポイントはこの形式で返却する。
 * @template T - レスポンスボディの型（data フィールド）
 */
export interface ApiResponse<T> {
  code:      string  // 結果コード（例: "SUCCESS", "ERROR_XXX"）
  message:   string  // 人間向けメッセージ
  data:      T       // 実際のデータ
  timestamp: string  // ISO 8601 形式のタイムスタンプ
}

/**
 * ページネーションレスポンスラッパー
 * 一覧取得エンドポイントで使用する。
 * @template T - リストアイテムの型
 */
export interface PageResponse<T> {
  content:       T[]    // 現在ページのアイテム一覧
  totalElements: number // 全件数
  totalPages:    number // 全ページ数
  page:          number // 現在ページ（0 始まり）
  size:          number // 1 ページあたりの件数
}

// =====================================================
// 認証
// =====================================================

/**
 * ログインリクエスト
 * POST /api/v1/auth/login のリクエストボディ
 */
export interface LoginRequest {
  username: string // ログインユーザー名
  password: string // パスワード（平文、TLS 経由で送信）
}

/**
 * ログインレスポンス
 * 認証成功時に返却される JWT トークンとユーザー情報
 */
export interface LoginResponse {
  token:       string                        // JWT アクセストークン
  tokenType:   string                        // トークン種別（通常 "Bearer"）
  expiresIn:   number                        // トークン有効期間（秒）
  userId:      number                        // ユーザー ID
  username:    string                        // ログインユーザー名
  displayName: string                        // 表示名
  role:        'ADMIN' | 'IT_STAFF' | 'VIEWER' // ユーザーロール
}

/**
 * ログイン中ユーザーの基本情報
 * Pinia ストアと localStorage に保持するユーザー情報の型。
 * セキュリティ上、最低限のフィールドのみを保持する。
 */
export interface CurrentUser {
  userId:      number                        // ユーザー ID
  username:    string                        // ログインユーザー名
  displayName: string                        // 表示名（ヘッダー等に使用）
  role:        'ADMIN' | 'IT_STAFF' | 'VIEWER' // ロール（権限判定に使用）
}

// =====================================================
// PC 資産
// =====================================================

/**
 * 取得区分
 * PURCHASE: 購入（自社所有）
 * RENTAL:   レンタル（レンタル業者から借用）
 */
export type AcquisitionType = 'PURCHASE' | 'RENTAL'

/**
 * PC ステータス
 * IN_USE:     使用中（誰かに割り当て済み）
 * IN_STORAGE: 保管中（未割り当て・在庫）
 * DISPOSED:   廃棄済み
 * IN_REPAIR:  修理中
 * RETURNED:   レンタル返却済み
 */
export type PcStatus = 'IN_USE' | 'IN_STORAGE' | 'DISPOSED' | 'IN_REPAIR' | 'RETURNED'

/** PC ステータスの表示ラベルマップ（UI 表示用） */
export const PC_STATUS_LABELS: Record<PcStatus, string> = {
  IN_USE:     '使用中',
  IN_STORAGE: '保管中',
  DISPOSED:   '廃棄済',
  IN_REPAIR:  '修理中',
  RETURNED:   '返却済',
}

/** 取得区分の表示ラベルマップ（UI 表示用） */
export const ACQUISITION_TYPE_LABELS: Record<AcquisitionType, string> = {
  PURCHASE: '購入',
  RENTAL:   'レンタル',
}

/**
 * PC 資産エンティティ
 * バックエンドの PcAsset テーブルに対応する型。
 */
export interface PcAsset {
  id:                   number          // 内部 ID（主キー）
  assetNumber:          string          // 資産番号（社内管理番号）
  deviceName:           string          // 機器名称
  deviceType:           string | null   // 機器種別（code_master DEVICE_TYPE のコード値。未設定は null）
  acquisitionType:      AcquisitionType // 取得区分（購入 / レンタル）
  maker:                string | null   // メーカー名
  modelNumber:          string | null   // 型番
  serialNumber:         string | null   // シリアル番号
  location:             string | null   // 設置場所
  status:               PcStatus        // 現在のステータス
  assignedEmployeeId:   number | null   // 割り当て中の社員 ID（null = 未割り当て）
  assignedEmployeeName: string | null   // 割り当て中の社員名（JOINで取得、表示優先度: 高）
  userName:             string | null   // エージェントが報告した使用者名テキスト（表示優先度: 低）
  agentNumber:          string | null   // エージェント番号（例: "AGT-A1B2C3D4"、エージェント未導入は null）
  hostname:             string | null   // PC のホスト名（エージェント連携用）
  agentLastSeen:        string | null   // エージェント最終通信日時（ISO 8601）
  note:                 string | null   // 備考
  createdAt:            string          // 登録日時（ISO 8601）
  updatedAt:            string          // 更新日時（ISO 8601）
}

/**
 * マルチセレクトフィルタの選択状態型
 * MultiSelectFilter コンポーネントの v-model として使用する。
 * チェックした項目を「含む」条件としてフィルタする。
 */
export interface MultiFilterValue {
  values: string[]  // 選択済みコード値の配列（空 = フィルタなし = 全件表示）
}

/** PC 資産一覧の検索パラメータ */
export interface AssetSearchParams {
  page?:             number   // ページ番号（0 始まり）
  size?:             number   // 1 ページあたりの件数
  keyword?:          string   // フリーワード検索（資産番号・機器名等）
  statuses?:         string   // ステータスフィルタ（カンマ区切り。例: "IN_USE,IN_STORAGE"）
  acquisitionTypes?: string   // 取得区分フィルタ（カンマ区切り）
  deviceTypes?:      string   // 機器種別フィルタ（カンマ区切り）
  location?:         string   // 設置場所フィルタ（部分一致）
}

/** PC 資産新規登録リクエスト */
export interface AssetCreateRequest {
  assetNumber:       string           // 資産番号（必須）
  deviceName:        string           // 機器名称（必須）
  deviceType?:       string           // 機器種別（任意。code_master DEVICE_TYPE のコード値）
  acquisitionType:   AcquisitionType  // 取得区分（必須）
  maker?:            string           // メーカー名
  modelNumber?:      string           // 型番
  serialNumber?:     string           // シリアル番号
  location?:         string           // 設置場所
  status?:           PcStatus         // ステータス（省略時はバックエンドのデフォルト値）
  assignedEmployeeId?: number | null  // 担当社員 ID
  hostname?:         string           // ホスト名
  note?:             string           // 備考
}

/** PC 資産更新リクエスト */
export interface AssetUpdateRequest {
  deviceName:        string           // 機器名称（必須）
  deviceType?:       string           // 機器種別（任意。code_master DEVICE_TYPE のコード値）
  acquisitionType:   AcquisitionType  // 取得区分（必須）
  maker?:            string           // メーカー名
  modelNumber?:      string           // 型番
  serialNumber?:     string           // シリアル番号
  location?:         string           // 設置場所
  status:            PcStatus         // ステータス（必須）
  assignedEmployeeId?: number | null  // 担当社員 ID
  hostname?:         string           // ホスト名
  note?:             string           // 備考
}

// =====================================================
// 社員
// =====================================================

/**
 * 社員エンティティ
 * バックエンドの Employee テーブルに対応する型。
 */
export interface Employee {
  id:           number        // 内部 ID（主キー）
  employeeCode: string        // 社員番号
  fullName:     string        // 氏名
  department:   string | null // 部署名
  position:     string | null // 役職
  email:        string | null // メールアドレス
  phone:        string | null // 電話番号
  location:     string | null // 所属拠点・フロア
  isActive:     boolean       // 在職フラグ（false = 退職済み）
  createdAt:    string        // 登録日時（ISO 8601）
  updatedAt:    string        // 更新日時（ISO 8601）
}

/** 社員一覧の検索パラメータ */
export interface EmployeeSearchParams {
  page?:     number          // ページ番号
  size?:     number          // 1 ページあたりの件数
  keyword?:  string          // フリーワード（社員番号・氏名等）
  isActive?: boolean | ''    // 在職フィルタ（'' = 全件）
}

/** 社員新規登録リクエスト */
export interface EmployeeCreateRequest {
  employeeCode: string  // 社員番号（必須・一意）
  fullName:     string  // 氏名（必須）
  department?:  string  // 部署名
  position?:    string  // 役職
  email?:       string  // メールアドレス
  phone?:       string  // 電話番号
  location?:    string  // 所属拠点
}

/** 社員更新リクエスト */
export interface EmployeeUpdateRequest {
  employeeCode?: string  // 社員コード（指定時のみ変更・一意チェックあり）
  fullName:      string  // 氏名（必須）
  department?:   string  // 部署名
  position?:     string  // 役職
  email?:        string  // メールアドレス
  phone?:        string  // 電話番号
  location?:     string  // 所属拠点
  isActive?:     boolean // 在職フラグ（false = 退職処理）
}

// =====================================================
// 貸出管理
// =====================================================

/**
 * PC 貸出エンティティ
 * バックエンドの PcLoan テーブルに対応する型。
 */
export interface PcLoan {
  id:                 number        // 内部 ID（主キー）
  pcAssetId:          number        // 貸出対象の PC 資産 ID
  assetNumber:        string        // 資産番号（表示用）
  deviceName:         string        // 機器名称（表示用）
  employeeId:         number        // 貸出先社員 ID
  employeeName:       string        // 貸出先社員名（表示用）
  employeeCode:       string        // 貸出先社員番号（表示用）
  loanDate:           string        // 貸出日（YYYY-MM-DD）
  expectedReturnDate: string | null // 返却予定日（YYYY-MM-DD、null = 未定）
  actualReturnDate:   string | null // 実際の返却日（null = 未返却）
  purpose:            string | null // 貸出目的
  note:               string | null // 備考
  createdBy:          number        // 登録者のユーザー ID
  createdByName:      string | null // 登録者名（表示用）
  createdAt:          string        // 登録日時（ISO 8601）
  returned:           boolean       // 返却済みフラグ
  overdue:            boolean       // 延滞フラグ（返却予定日を超過かつ未返却）
}

/** 貸出登録リクエスト */
export interface LoanCreateRequest {
  pcAssetId:           number  // 貸出対象の PC 資産 ID（必須）
  employeeId:          number  // 貸出先社員 ID（必須）
  loanDate:            string  // 貸出日（YYYY-MM-DD、必須）
  expectedReturnDate?: string  // 返却予定日（YYYY-MM-DD）
  purpose?:            string  // 貸出目的
  note?:               string  // 備考
}

/** 返却処理リクエスト */
export interface LoanReturnRequest {
  actualReturnDate: string  // 実際の返却日（YYYY-MM-DD、必須）
  note?:            string  // 備考（返却時のメモ等）
}

/** 貸出一覧の検索パラメータ */
export interface LoanSearchParams {
  page?:     number   // ページ番号
  size?:     number   // 1 ページあたりの件数
  keyword?:  string   // フリーワード検索
  returned?: boolean  // 返却済みフィルタ（undefined = 全件、false = 未返却のみ）
}

// =====================================================
// レンタル管理
// =====================================================

/**
 * PC レンタル契約エンティティ
 * バックエンドの PcRental テーブルに対応する型。
 */
export interface PcRental {
  id:               number        // 内部 ID（主キー）
  pcAssetId:        number        // 対象の PC 資産 ID
  assetNumber:      string        // 資産番号（表示用）
  deviceName:       string        // 機器名称（表示用）
  rentalVendorId:   number        // レンタル業者 ID
  vendorName:       string        // 業者名（表示用）
  hostname:         string | null // PC のホスト名（pc_assets.hostname から取得）
  contractNumber:   string | null // 契約番号
  rentalStartDate:  string        // レンタル開始日（YYYY-MM-DD）
  rentalEndDate:    string        // レンタル終了日（YYYY-MM-DD）
  monthlyFee:       number | null // 月額費用（円）
  contractFilePath: string | null // 契約書ファイルパス
  returnDate:       string | null // 実際の返却日（null = 未返却）
  createdAt:        string        // 登録日時（ISO 8601）
  updatedAt:        string        // 更新日時（ISO 8601）
  returned:         boolean       // 返却済みフラグ
  expired:          boolean       // 契約期限切れフラグ
  daysUntilExpiry:  number | null // 終了日までの残日数（マイナス = 超過）
}

/**
 * レンタル業者エンティティ
 * バックエンドの RentalVendor テーブルに対応する型。
 */
export interface RentalVendor {
  id:          number        // 内部 ID（主キー）
  companyName: string        // 業者会社名
  contactName: string | null // 担当者名
  phone:       string | null // 電話番号
  email:       string | null // メールアドレス
  address:     string | null // 住所
  note:        string | null // 備考
  createdAt:   string        // 登録日時（ISO 8601）
  updatedAt:   string        // 更新日時（ISO 8601）
}

/** レンタル契約登録リクエスト */
export interface RentalCreateRequest {
  pcAssetId:        number  // 対象の PC 資産 ID（必須）
  rentalVendorId:   number  // レンタル業者 ID（必須）
  contractNumber?:  string  // 契約番号
  rentalStartDate:  string  // 開始日（YYYY-MM-DD、必須）
  rentalEndDate:    string  // 終了日（YYYY-MM-DD、必須）
  monthlyFee?:      number  // 月額費用（円）
  contractFilePath?: string // 契約書ファイルパス
}

/**
 * レンタル契約更新リクエスト
 * PUT /api/v1/rentals/{id} のリクエストボディ。
 * PC 資産の変更は不可のため pcAssetId は含めない。
 */
export interface RentalUpdateRequest {
  rentalVendorId:  number         // レンタルベンダー ID（必須）
  contractNumber:  string         // 契約番号（空文字で未設定）
  rentalStartDate: string         // 開始日（YYYY-MM-DD、必須）
  rentalEndDate:   string         // 終了日（YYYY-MM-DD、必須）
  monthlyFee:      number | null  // 月額費用（円、null = 未設定）
}

/**
 * レンタル返却リクエスト
 * PUT /api/v1/rentals/{id}/return のリクエストボディ。
 */
export interface RentalReturnRequest {
  returnDate: string  // 返却日（YYYY-MM-DD、必須）
}

/**
 * レンタル契約変更履歴エントリ
 * GET /api/v1/rentals/{id}/histories のレスポンスアイテム型。
 * フロントエンドでは operationId をキーにグルーピングして表示する。
 */
export interface RentalHistoryEntry {
  id:            number          // レコードID
  rentalId:      number          // 対象レンタル契約ID
  operationId:   string          // 同一操作をまとめるUUID（グルーピングキー）
  operation:     string          // CREATE / UPDATE / RETURN
  fieldName:     string | null   // 変更フィールド名（UPDATE 時のみ）
  fieldLabel:    string | null   // 画面表示用ラベル
  oldValue:      string | null   // 変更前の値
  newValue:      string | null   // 変更後の値
  changedByName: string | null   // 操作者名
  changedAt:     string          // ISO 8601 形式の変更日時
}

/** レンタル業者登録リクエスト */
export interface RentalVendorCreateRequest {
  companyName:  string  // 業者会社名（必須）
  contactName?: string  // 担当者名
  phone?:       string  // 電話番号
  email?:       string  // メールアドレス
  address?:     string  // 住所
  note?:        string  // 備考
}

/**
 * インストール済みソフトウェア エンティティ
 * GET /api/v1/assets/{id}/software のレスポンスアイテム型。
 */
export interface InstalledSoftware {
  id:           number        // レコードID
  softwareName: string        // ソフトウェア名
  version:      string | null // バージョン（未取得の場合は null）
  publisher:    string | null // 発行元（未取得の場合は null）
}

/** レンタル一覧の検索パラメータ */
export interface RentalSearchParams {
  page?:         number              // ページ番号
  size?:         number              // 1 ページあたりの件数
  keyword?:      string              // フリーワード検索
  returned?:     boolean             // 返却済みフィルタ
  expiryFilter?: 'near' | 'expired'  // 期限フィルタ（near: まもなく期限切れ、expired: 期限切れ）
}

// =====================================================
// ソフトウェアライセンス
// =====================================================

/**
 * ソフトウェアライセンスエンティティ
 * バックエンドの SoftwareLicense テーブルに対応する型。
 */
export interface SoftwareLicense {
  id:             number        // 内部 ID（主キー）
  softwareName:   string        // ソフトウェア名
  publisher:      string | null // 発行元（ベンダー名）
  licenseType:    string | null // ライセンス種別（例: "ボリューム", "サブスクリプション"）
  purchasedCount: number        // 購入ライセンス数
  installedCount: number        // インストール済み台数（エージェント収集値）
  note:           string | null // 備考
  createdAt:      string        // 登録日時（ISO 8601）
  updatedAt:      string        // 更新日時（ISO 8601）
  overLimit:      boolean       // ライセンス超過フラグ（installedCount > purchasedCount）
}

/** ソフトウェアライセンス登録リクエスト */
export interface SoftwareCreateRequest {
  softwareName:    string  // ソフトウェア名（必須）
  publisher?:      string  // 発行元
  licenseType?:    string  // ライセンス種別
  purchasedCount?: number  // 購入ライセンス数（省略時は 0）
  note?:           string  // 備考
}

/** ソフトウェアライセンス一覧の検索パラメータ */
export interface SoftwareSearchParams {
  page?:      number   // ページ番号
  size?:      number   // 1 ページあたりの件数
  keyword?:   string   // フリーワード検索（ソフトウェア名・発行元等）
  overLimit?: boolean  // 超過フィルタ（true = ライセンス超過のみ表示）
}

// =====================================================
// ユーザー管理
// =====================================================

/**
 * システムユーザーエンティティ
 * バックエンドの SystemUser テーブルに対応する型。
 * PC 管理システムにログインするアカウント情報。
 */
export interface SystemUser {
  id:          number                        // 内部 ID（主キー）
  username:    string                        // ログインユーザー名
  displayName: string                        // 表示名
  role:        'ADMIN' | 'IT_STAFF' | 'VIEWER' // ロール
  email:       string | null                 // メールアドレス
  isActive:    boolean                       // 有効フラグ（false = 無効化済み）
  lastLoginAt: string | null                 // 最終ログイン日時（ISO 8601）
  createdAt:   string                        // 登録日時（ISO 8601）
  updatedAt:   string | null                 // 更新日時（ISO 8601）
}

/** システムユーザー登録リクエスト */
export interface UserCreateRequest {
  username:    string                        // ユーザー名（必須・一意）
  password:    string                        // 初期パスワード（必須）
  displayName: string                        // 表示名（必須）
  role:        'ADMIN' | 'IT_STAFF' | 'VIEWER' // ロール（必須）
  email?:      string                        // メールアドレス
}

/** システムユーザー更新リクエスト */
export interface UserUpdateRequest {
  displayName: string                        // 表示名（必須）
  role:        'ADMIN' | 'IT_STAFF' | 'VIEWER' // ロール（必須）
  email?:      string                        // メールアドレス
  isActive?:   boolean                       // 有効フラグ（false = 無効化）
  password?:   string                        // パスワード変更時のみ指定
}

// =====================================================
// ダッシュボード
// =====================================================

/**
 * ダッシュボード統計情報
 * トップページに表示するサマリー数値。
 */
export interface DashboardStats {
  totalPcCount:           number // 総 PC 台数
  inUsePcCount:           number // 使用中 PC 台数
  inStoragePcCount:       number // 保管中 PC 台数
  activeLoansCount:       number // 貸出中件数（未返却）
  nearExpiryRentalsCount: number // まもなく期限切れのレンタル件数
  expiredRentalsCount:    number // 期限切れレンタル件数
  licenseOverCount:       number // ライセンス超過ソフトウェア件数
}

// =====================================================
// エージェント登録トークン
// =====================================================

/**
 * エージェント登録トークン
 * エージェントの初回登録に使用する1回限り・24時間有効のトークン。
 */
export interface EnrollmentToken {
  id:                 number             // レコードID
  token:              string             // トークン文字列（UUID形式）
  expiresAt:          string             // 有効期限（ISO 8601）
  status:             'UNUSED' | 'USED' | 'EXPIRED' // トークン状態
  usedByAgentNumber:  string | null      // 使用したエージェント番号（使用済みの場合）
  usedAt:             string | null      // 使用日時（使用済みの場合）
  note:               string | null      // 発行メモ（対象PC名など）
  createdAt:          string             // 発行日時（ISO 8601）
}

/** 登録トークン発行リクエスト */
export interface EnrollmentTokenCreateRequest {
  note?: string  // 発行メモ（任意）
}

