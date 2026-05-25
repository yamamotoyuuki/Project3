/**
 * api/auth.ts
 * -----------------------------------------------
 * 認証 API モジュール
 *
 * バックエンドの /api/v1/auth エンドポイントと通信し、
 * ログイン・ログアウト処理を担う。
 * 共通 apiClient を使用することで JWT 付与などは自動適用される。
 * -----------------------------------------------
 */
import apiClient from './axios'
import type { ApiResponse, LoginRequest, LoginResponse } from '@/types'

export const authApi = {
  /**
   * ログイン
   * @param request - ユーザー名とパスワードを含むログインリクエスト
   * @returns JWT トークン・ユーザー情報を含むレスポンス
   * @throws AxiosError - 認証失敗（401）やサーバーエラー時にスロー
   */
  login(request: LoginRequest): Promise<ApiResponse<LoginResponse>> {
    // POST /api/v1/auth/login にリクエストを送信し、レスポンスボディ（data）を返す
    return apiClient.post<ApiResponse<LoginResponse>>('/auth/login', request)
      .then(res => res.data)
  },

  /**
   * ログアウト
   * バックエンド側でトークンの無効化を行う（ステートレスJWTの場合は任意）。
   * @returns void（レスポンスボディは使用しない）
   */
  logout(): Promise<void> {
    // POST /api/v1/auth/logout にリクエストを送信
    return apiClient.post('/auth/logout').then(() => {})
  },
}
