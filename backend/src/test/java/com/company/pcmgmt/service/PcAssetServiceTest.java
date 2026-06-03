package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.asset.AssetCreateRequest;
import com.company.pcmgmt.api.dto.request.asset.AssetSearchRequest;
import com.company.pcmgmt.api.dto.request.asset.AssetUpdateRequest;
import com.company.pcmgmt.api.dto.response.asset.AssetResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.domain.entity.PcAsset;
import com.company.pcmgmt.domain.mapper.asset.PcAssetMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import com.company.pcmgmt.service.asset.PcAssetService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

/**
 * PcAssetService の単体テスト
 *
 * <p>Mockito を使用して {@link PcAssetMapper} をモック化し、
 * ビジネスロジック層のみを独立してテストする。</p>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PcAssetService")
class PcAssetServiceTest {

    /** テスト対象（SUT: System Under Test） */
    @InjectMocks
    private PcAssetService sut;

    /** モック化した DB アクセス層 */
    @Mock
    private PcAssetMapper pcAssetMapper;

    // =====================================================
    // テストデータファクトリメソッド
    // =====================================================

    /** テスト用の AssetResponse を生成するファクトリメソッド */
    private AssetResponse buildAssetResponse(Long id, String assetNumber) {
        AssetResponse res = new AssetResponse();
        res.setId(id);
        res.setAssetNumber(assetNumber);
        res.setDeviceName("テスト端末");
        res.setAcquisitionType("PURCHASE");
        res.setStatus("IN_STORAGE");
        return res;
    }

    /** テスト用の PcAsset エンティティを生成するファクトリメソッド */
    private PcAsset buildPcAsset(Long id, String assetNumber) {
        PcAsset asset = new PcAsset();
        asset.setId(id);
        asset.setAssetNumber(assetNumber);
        asset.setDeviceName("テスト端末");
        asset.setAcquisitionType("PURCHASE");
        asset.setStatus("IN_STORAGE");
        return asset;
    }

    // =====================================================
    // findAll テスト
    // =====================================================

    @Nested
    @DisplayName("findAll()")
    class FindAll {

        @Test
        @DisplayName("検索結果を PageResponse に変換して返す")
        void returns_page_response() {
            // Arrange: モックの返却値を設定
            AssetSearchRequest req = new AssetSearchRequest();
            req.setPage(0);
            req.setSize(10);

            List<AssetResponse> content = List.of(
                    buildAssetResponse(1L, "PC-001"),
                    buildAssetResponse(2L, "PC-002")
            );
            given(pcAssetMapper.findAll(any())).willReturn(content);
            given(pcAssetMapper.countAll(any())).willReturn(2L);

            // Act: テスト対象メソッドを実行
            PageResponse<AssetResponse> result = sut.findAll(req);

            // Assert: 期待通りの PageResponse が返ること
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2L);
            assertThat(result.getTotalPages()).isEqualTo(1);
        }
    }

    // =====================================================
    // findById テスト
    // =====================================================

    @Nested
    @DisplayName("findById()")
    class FindById {

        @Test
        @DisplayName("存在するIDを指定すると AssetResponse を返す")
        void returns_asset_when_found() {
            // Arrange
            given(pcAssetMapper.findById(1L)).willReturn(buildAssetResponse(1L, "PC-001"));

            // Act
            AssetResponse result = sut.findById(1L);

            // Assert
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getAssetNumber()).isEqualTo("PC-001");
        }

        @Test
        @DisplayName("存在しないIDを指定すると ResourceNotFoundException がスローされる")
        void throws_when_not_found() {
            // Arrange: Mapper が null を返す（DB に存在しない）
            given(pcAssetMapper.findById(999L)).willReturn(null);

            // Act & Assert: 例外がスローされること
            assertThatThrownBy(() -> sut.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("999");
        }
    }

    // =====================================================
    // create テスト
    // =====================================================

    @Nested
    @DisplayName("create()")
    class Create {

        @Test
        @DisplayName("正常な登録リクエストで AssetResponse を返す")
        void creates_asset_successfully() {
            // Arrange
            AssetCreateRequest req = new AssetCreateRequest();
            req.setAssetNumber("PC-NEW-001");
            req.setDeviceName("新しい端末");
            req.setAcquisitionType("PURCHASE");

            // 資産番号の重複なし
            given(pcAssetMapper.existsByAssetNumber("PC-NEW-001", null)).willReturn(false);
            // INSERT は void 相当（int 戻り値）
            given(pcAssetMapper.insert(any())).willReturn(1);
            // INSERT 後の findById で登録済みデータを返す
            given(pcAssetMapper.findById(any())).willReturn(buildAssetResponse(10L, "PC-NEW-001"));

            // Act
            AssetResponse result = sut.create(req);

            // Assert
            assertThat(result.getAssetNumber()).isEqualTo("PC-NEW-001");
            then(pcAssetMapper).should().insert(any(PcAsset.class));
        }

        @Test
        @DisplayName("資産番号が重複している場合は IllegalArgumentException がスローされる")
        void throws_when_asset_number_duplicated() {
            // Arrange: 資産番号が既に存在する
            AssetCreateRequest req = new AssetCreateRequest();
            req.setAssetNumber("PC-DUPLICATE");
            req.setDeviceName("テスト端末");
            req.setAcquisitionType("PURCHASE");

            given(pcAssetMapper.existsByAssetNumber("PC-DUPLICATE", null)).willReturn(true);

            // Act & Assert: 例外がスローされること
            assertThatThrownBy(() -> sut.create(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("PC-DUPLICATE");

            // Mapper の insert が呼ばれていないこと（ロールバック確認）
            then(pcAssetMapper).should(never()).insert(any());
        }
    }

    // =====================================================
    // update テスト
    // =====================================================

    @Nested
    @DisplayName("update()")
    class Update {

        @Test
        @DisplayName("存在するIDを指定すると更新後の AssetResponse を返す")
        void updates_asset_successfully() {
            // Arrange
            PcAsset existing = buildPcAsset(1L, "PC-001");
            given(pcAssetMapper.findRawById(1L)).willReturn(existing);
            given(pcAssetMapper.update(any())).willReturn(1);

            AssetResponse updated = buildAssetResponse(1L, "PC-001");
            updated.setStatus("IN_USE");
            given(pcAssetMapper.findById(1L)).willReturn(updated);

            AssetUpdateRequest req = new AssetUpdateRequest();
            req.setDeviceName("更新後の端末名");
            req.setAcquisitionType("PURCHASE");
            req.setStatus("IN_USE");

            // Act
            AssetResponse result = sut.update(1L, req);

            // Assert
            assertThat(result.getStatus()).isEqualTo("IN_USE");
            then(pcAssetMapper).should().update(any(PcAsset.class));
        }

        @Test
        @DisplayName("存在しないIDを指定すると ResourceNotFoundException がスローされる")
        void throws_when_not_found() {
            // Arrange
            given(pcAssetMapper.findRawById(999L)).willReturn(null);

            AssetUpdateRequest req = new AssetUpdateRequest();
            req.setDeviceName("テスト");
            req.setAcquisitionType("PURCHASE");
            req.setStatus("IN_STORAGE");

            // Act & Assert
            assertThatThrownBy(() -> sut.update(999L, req))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    // =====================================================
    // delete テスト
    // =====================================================

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("存在するIDを指定すると削除処理が実行される")
        void deletes_asset_successfully() {
            // Arrange
            given(pcAssetMapper.findRawById(1L)).willReturn(buildPcAsset(1L, "PC-001"));
            given(pcAssetMapper.deleteById(1L)).willReturn(1);

            // Act
            sut.delete(1L);

            // Assert: deleteById が1回呼ばれること
            then(pcAssetMapper).should().deleteById(1L);
        }

        @Test
        @DisplayName("存在しないIDを指定すると ResourceNotFoundException がスローされる")
        void throws_when_not_found() {
            // Arrange
            given(pcAssetMapper.findRawById(999L)).willReturn(null);

            // Act & Assert
            assertThatThrownBy(() -> sut.delete(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            // deleteById が呼ばれていないこと
            then(pcAssetMapper).should(never()).deleteById(anyLong());
        }
    }
}
