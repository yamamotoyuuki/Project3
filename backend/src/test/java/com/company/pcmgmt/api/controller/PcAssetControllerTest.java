package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.controller.asset.PcAssetController;
import com.company.pcmgmt.api.dto.request.asset.AssetCreateRequest;
import com.company.pcmgmt.api.dto.response.asset.AssetResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import com.company.pcmgmt.service.asset.PcAssetService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * PcAssetController の単体テスト（MockMvc を使用したスライステスト）
 *
 * <p>{@link WebMvcTest} で Web 層のみをロードし、
 * {@link PcAssetService} は Mockito でモック化する。</p>
 *
 * <p>テスト観点:</p>
 * <ul>
 *   <li>HTTP ステータスコードの正確性</li>
 *   <li>レスポンス JSON の構造</li>
 *   <li>バリデーションエラー時の 400 返却</li>
 *   <li>存在しないリソースへのアクセス時の 404 返却</li>
 * </ul>
 */
@WebMvcTest(PcAssetController.class)
@ActiveProfiles("test")
@DisplayName("PcAssetController")
class PcAssetControllerTest {

    /** HTTP リクエストのシミュレーションに使用する MockMvc */
    @Autowired
    private MockMvc mockMvc;

    /** JSON 変換に使用する ObjectMapper */
    @Autowired
    private ObjectMapper objectMapper;

    /** モック化したサービス層 */
    @MockBean
    private PcAssetService pcAssetService;

    // =====================================================
    // テストデータファクトリメソッド
    // =====================================================

    /** テスト用 AssetResponse を生成する */
    private AssetResponse buildAssetResponse(Long id, String assetNumber) {
        AssetResponse res = new AssetResponse();
        res.setId(id);
        res.setAssetNumber(assetNumber);
        res.setDeviceName("テスト端末");
        res.setAcquisitionType("PURCHASE");
        res.setStatus("IN_STORAGE");
        return res;
    }

    // =====================================================
    // GET /api/v1/assets テスト
    // =====================================================

    @Nested
    @DisplayName("GET /api/v1/assets")
    class GetAll {

        @Test
        @WithMockUser  // 認証済みユーザーとして実行
        @DisplayName("200 OK と PageResponse を返す")
        void returns_200_with_page_response() throws Exception {
            // Arrange: サービスの返却値を設定
            PageResponse<AssetResponse> page = PageResponse.of(
                    List.of(buildAssetResponse(1L, "PC-001")), 1L, 0, 20
            );
            given(pcAssetService.findAll(any())).willReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/v1/assets"))
                    .andExpect(status().isOk())                           // HTTP 200
                    .andExpect(jsonPath("$.code").value("SUCCESS"))       // コード確認
                    .andExpect(jsonPath("$.data.totalElements").value(1)) // 総件数確認
                    .andExpect(jsonPath("$.data.content[0].assetNumber").value("PC-001")); // 資産番号確認
        }

        @Test
        @DisplayName("未認証の場合は 401 を返す")
        void returns_401_when_unauthenticated() throws Exception {
            mockMvc.perform(get("/api/v1/assets"))
                    .andExpect(status().isUnauthorized()); // HTTP 401
        }
    }

    // =====================================================
    // GET /api/v1/assets/{id} テスト
    // =====================================================

    @Nested
    @DisplayName("GET /api/v1/assets/{id}")
    class GetById {

        @Test
        @WithMockUser
        @DisplayName("存在するIDで 200 OK と AssetResponse を返す")
        void returns_200_when_found() throws Exception {
            // Arrange
            given(pcAssetService.findById(1L)).willReturn(buildAssetResponse(1L, "PC-001"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/assets/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.assetNumber").value("PC-001"));
        }

        @Test
        @WithMockUser
        @DisplayName("存在しないIDで 404 Not Found を返す")
        void returns_404_when_not_found() throws Exception {
            // Arrange: サービスが ResourceNotFoundException をスロー
            given(pcAssetService.findById(999L))
                    .willThrow(new ResourceNotFoundException("PC資産が見つかりません: id=999"));

            // Act & Assert
            mockMvc.perform(get("/api/v1/assets/999"))
                    .andExpect(status().isNotFound()); // HTTP 404
        }
    }

    // =====================================================
    // POST /api/v1/assets テスト
    // =====================================================

    @Nested
    @DisplayName("POST /api/v1/assets")
    class Create {

        @Test
        @WithMockUser
        @DisplayName("正常なリクエストで 201 Created と AssetResponse を返す")
        void returns_201_when_created() throws Exception {
            // Arrange: 登録リクエスト
            AssetCreateRequest req = new AssetCreateRequest();
            req.setAssetNumber("PC-NEW-001");
            req.setDeviceName("新しい端末");
            req.setAcquisitionType("PURCHASE");

            // サービスの返却値
            given(pcAssetService.create(any())).willReturn(buildAssetResponse(10L, "PC-NEW-001"));

            // Act & Assert
            mockMvc.perform(
                            post("/api/v1/assets")
                                    .with(csrf())                              // CSRF トークンを付与
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(req))
                    )
                    .andExpect(status().isCreated())                           // HTTP 201
                    .andExpect(jsonPath("$.data.assetNumber").value("PC-NEW-001"));
        }

        @Test
        @WithMockUser
        @DisplayName("必須フィールドが欠落している場合は 400 Bad Request を返す")
        void returns_400_when_invalid_request() throws Exception {
            // Arrange: 空のリクエスト（必須フィールドなし）
            AssetCreateRequest emptyReq = new AssetCreateRequest();

            // Act & Assert
            mockMvc.perform(
                            post("/api/v1/assets")
                                    .with(csrf())
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(objectMapper.writeValueAsString(emptyReq))
                    )
                    .andExpect(status().isBadRequest()); // HTTP 400
        }
    }

    // =====================================================
    // DELETE /api/v1/assets/{id} テスト
    // =====================================================

    @Nested
    @DisplayName("DELETE /api/v1/assets/{id}")
    class Delete {

        @Test
        @WithMockUser
        @DisplayName("存在するIDで 200 OK を返す")
        void returns_200_when_deleted() throws Exception {
            // Arrange: 削除は void（例外なし）
            willDoNothing().given(pcAssetService).delete(1L);

            // Act & Assert
            mockMvc.perform(
                            delete("/api/v1/assets/1")
                                    .with(csrf())
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message").value("PC資産を削除しました"));
        }

        @Test
        @WithMockUser
        @DisplayName("存在しないIDで 404 Not Found を返す")
        void returns_404_when_not_found() throws Exception {
            // Arrange: サービスが ResourceNotFoundException をスロー
            willThrow(new ResourceNotFoundException("PC資産が見つかりません: id=999"))
                    .given(pcAssetService).delete(999L);

            // Act & Assert
            mockMvc.perform(
                            delete("/api/v1/assets/999")
                                    .with(csrf())
                    )
                    .andExpect(status().isNotFound());
        }
    }
}
