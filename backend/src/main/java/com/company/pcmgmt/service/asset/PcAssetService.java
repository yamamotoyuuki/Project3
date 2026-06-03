package com.company.pcmgmt.service.asset;

import com.company.pcmgmt.api.dto.request.asset.AssetCreateRequest;
import com.company.pcmgmt.api.dto.request.asset.AssetSearchRequest;
import com.company.pcmgmt.api.dto.request.asset.AssetUpdateRequest;
import com.company.pcmgmt.api.dto.response.asset.AssetResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.domain.entity.PcAsset;
import com.company.pcmgmt.domain.mapper.asset.PcAssetMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * PC資産管理サービス
 *
 * <p>PC資産の CRUD 操作を担当するビジネスロジック層。
 * データアクセスは {@link PcAssetMapper} に委譲する。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PcAssetService {

    /** PC資産の DB アクセスを担うマッパー */
    private final PcAssetMapper pcAssetMapper;

    /**
     * PC資産一覧をページネーション・絞り込み条件付きで取得する
     *
     * @param req 検索条件（キーワード・ステータス・取得区分・場所・ページ情報）
     * @return ページネーション付きPC資産レスポンス
     */
    @Transactional(readOnly = true)
    public PageResponse<AssetResponse> findAll(AssetSearchRequest req) {
        // 検索条件に一致するデータを取得
        List<AssetResponse> content = pcAssetMapper.findAll(req);
        // 総件数を取得（ページネーション計算用）
        long total = pcAssetMapper.countAll(req);
        return PageResponse.of(content, total, req.getPage(), req.getSize());
    }

    /**
     * 指定IDのPC資産詳細を取得する
     *
     * @param id PC資産ID
     * @return AssetResponse
     * @throws ResourceNotFoundException 指定IDのPC資産が存在しない場合
     */
    @Transactional(readOnly = true)
    public AssetResponse findById(Long id) {
        AssetResponse asset = pcAssetMapper.findById(id);
        // 存在しない場合は 404 例外をスロー
        if (asset == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + id);
        }
        return asset;
    }

    /**
     * PC資産を新規登録する
     *
     * @param req 登録リクエスト
     * @return 登録後のPC資産レスポンス
     * @throws IllegalArgumentException 資産番号が既に使用されている場合
     */
    @Transactional
    public AssetResponse create(AssetCreateRequest req) {
        // 資産番号の重複チェック（excludeId=null は新規登録を意味する）
        if (pcAssetMapper.existsByAssetNumber(req.getAssetNumber(), null)) {
            throw new IllegalArgumentException("資産番号 [" + req.getAssetNumber() + "] は既に使用されています");
        }

        // リクエストからエンティティを組み立て
        PcAsset asset = new PcAsset();
        asset.setAssetNumber(req.getAssetNumber());       // 資産番号
        asset.setDeviceName(req.getDeviceName());         // 端末名
        // 機器種別（空文字は null に変換して未選択として保存する）
        asset.setDeviceType(
            (req.getDeviceType() != null && !req.getDeviceType().isBlank())
                ? req.getDeviceType() : null
        );
        asset.setAcquisitionType(req.getAcquisitionType()); // 取得区分
        asset.setMaker(req.getMaker());                   // メーカー
        asset.setModelNumber(req.getModelNumber());       // 型番
        asset.setSerialNumber(req.getSerialNumber());     // シリアル番号
        asset.setLocation(req.getLocation());             // 設置場所
        // ステータスが未指定の場合は「保管中」をデフォルト値として使用
        asset.setStatus(req.getStatus() != null ? req.getStatus() : "IN_STORAGE");
        asset.setAssignedEmployeeId(req.getAssignedEmployeeId()); // 担当社員ID
        asset.setHostname(req.getHostname());             // ホスト名
        asset.setNote(req.getNote());                     // 備考

        // DBに保存（INSERT 後、asset.id に自動採番IDがセットされる）
        pcAssetMapper.insert(asset);
        log.info("PC資産登録: assetNumber={}", asset.getAssetNumber());

        // 保存後のデータ（担当社員名等JOINデータを含む）を返す
        return pcAssetMapper.findById(asset.getId());
    }

    /**
     * PC資産を更新する
     *
     * @param id  更新対象のPC資産ID
     * @param req 更新リクエスト
     * @return 更新後のPC資産レスポンス
     * @throws ResourceNotFoundException 指定IDのPC資産が存在しない場合
     */
    @Transactional
    public AssetResponse update(Long id, AssetUpdateRequest req) {
        // 更新対象の存在確認（存在しない場合は 404 例外をスロー）
        PcAsset existing = pcAssetMapper.findRawById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + id);
        }

        // 既存エンティティに更新値をセット
        existing.setDeviceName(req.getDeviceName());           // 端末名
        // 機器種別（空文字は null に変換して未選択として保存する）
        existing.setDeviceType(
            (req.getDeviceType() != null && !req.getDeviceType().isBlank())
                ? req.getDeviceType() : null
        );
        existing.setAcquisitionType(req.getAcquisitionType()); // 取得区分
        existing.setMaker(req.getMaker());                     // メーカー
        existing.setModelNumber(req.getModelNumber());         // 型番
        existing.setSerialNumber(req.getSerialNumber());       // シリアル番号
        // location（設置場所）はエージェントが自動更新するため手動編集では変更しない
        existing.setStatus(req.getStatus());                   // ステータス
        existing.setAssignedEmployeeId(req.getAssignedEmployeeId()); // 担当社員ID
        existing.setHostname(req.getHostname());               // ホスト名
        existing.setNote(req.getNote());                       // 備考

        pcAssetMapper.update(existing);
        log.info("PC資産更新: id={}", id);

        // 更新後のデータを返す
        return pcAssetMapper.findById(id);
    }

    /**
     * PC資産を削除する（物理削除）
     *
     * <p>廃棄フローを想定した物理削除。論理削除が必要な場合はステータスを DISPOSED に変更すること。</p>
     *
     * @param id 削除対象のPC資産ID
     * @throws ResourceNotFoundException 指定IDのPC資産が存在しない場合
     */
    @Transactional
    public void delete(Long id) {
        // 削除対象の存在確認（存在しない場合は 404 例外をスロー）
        PcAsset existing = pcAssetMapper.findRawById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + id);
        }
        pcAssetMapper.deleteById(id);
        log.info("PC資産削除: id={}", id);
    }
}
