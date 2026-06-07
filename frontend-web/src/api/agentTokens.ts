/**
 * api/agentTokens.ts
 * -----------------------------------------------
 * エージェント登録トークン管理 API クライアント
 *
 * バックエンドの /api/v1/agent-tokens エンドポイントと通信する。
 * -----------------------------------------------
 */
import apiClient from './axios'
import type { ApiResponse, EnrollmentToken, EnrollmentTokenCreateRequest } from '@/types'

export const agentTokensApi = {
  /**
   * 登録トークンを新規発行する
   * POST /api/v1/agent-tokens
   */
  issue(req: EnrollmentTokenCreateRequest): Promise<ApiResponse<EnrollmentToken>> {
    return apiClient.post<ApiResponse<EnrollmentToken>>('/agent-tokens', req).then(r => r.data)
  },

  /**
   * 全登録トークンを一覧取得する
   * GET /api/v1/agent-tokens
   */
  findAll(): Promise<ApiResponse<EnrollmentToken[]>> {
    return apiClient.get<ApiResponse<EnrollmentToken[]>>('/agent-tokens').then(r => r.data)
  },

  /**
   * 指定IDのトークンを削除（強制無効化）する
   * DELETE /api/v1/agent-tokens/{id}
   */
  delete(id: number): Promise<ApiResponse<void>> {
    return apiClient.delete<ApiResponse<void>>(`/agent-tokens/${id}`).then(r => r.data)
  },
}
