/**
 * api/common.ts
 * -----------------------------------------------
 * 共通コードマスタ API クライアント
 *
 * GET /api/v1/common/codes/{codeType} を呼び出し、
 * ドロップダウンリスト用のコード値（コード値と表示ラベルのペア）を取得する。
 *
 * 取得条件（バックエンド側で適用）:
 *   - is_active = 1 の有効レコードのみ
 *   - sort_order ASC で表示順に並べ替え
 * -----------------------------------------------
 */
import apiClient from './axios'
import type { ApiResponse, CodeValue } from '@/types'

export const commonApi = {
  /**
   * 指定したコード区分の有効なコード値一覧を取得する。
   *
   * @param codeType - コード区分キー（例: "ACQUISITION_TYPE", "PC_STATUS", "DEVICE_TYPE"）
   * @returns コード値と表示ラベルのペアのリスト（sort_order 昇順）
   */
  getCodeValues(codeType: string): Promise<ApiResponse<CodeValue[]>> {
    return apiClient
      .get<ApiResponse<CodeValue[]>>(`/common/codes/${codeType}`)
      .then((res) => res.data)
  },
}
