package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.AssetCreateRequest;
import com.company.pcmgmt.api.dto.request.AssetSearchRequest;
import com.company.pcmgmt.api.dto.request.AssetUpdateRequest;
import com.company.pcmgmt.api.dto.response.AssetResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.domain.entity.PcAsset;
import com.company.pcmgmt.domain.mapper.PcAssetMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PcAssetService {

    private final PcAssetMapper pcAssetMapper;

    /**
     * PC資産一覧取得（ページネーション・絞り込み）
     */
    @Transactional(readOnly = true)
    public PageResponse<AssetResponse> findAll(AssetSearchRequest req) {
        List<AssetResponse> content = pcAssetMapper.findAll(req);
        long total = pcAssetMapper.countAll(req);
        return PageResponse.of(content, total, req.getPage(), req.getSize());
    }

    /**
     * PC資産詳細取得
     */
    @Transactional(readOnly = true)
    public AssetResponse findById(Long id) {
        AssetResponse asset = pcAssetMapper.findById(id);
        if (asset == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + id);
        }
        return asset;
    }

    /**
     * PC資産登録
     */
    @Transactional
    public AssetResponse create(AssetCreateRequest req) {
        // 資産番号重複チェック
        if (pcAssetMapper.existsByAssetNumber(req.getAssetNumber(), null)) {
            throw new IllegalArgumentException("資産番号 [" + req.getAssetNumber() + "] は既に使用されています");
        }

        PcAsset asset = new PcAsset();
        asset.setAssetNumber(req.getAssetNumber());
        asset.setDeviceName(req.getDeviceName());
        asset.setAcquisitionType(req.getAcquisitionType());
        asset.setMaker(req.getMaker());
        asset.setModelNumber(req.getModelNumber());
        asset.setSerialNumber(req.getSerialNumber());
        asset.setLocation(req.getLocation());
        asset.setStatus(req.getStatus() != null ? req.getStatus() : "IN_STORAGE");
        asset.setAssignedEmployeeId(req.getAssignedEmployeeId());
        asset.setHostname(req.getHostname());
        asset.setNote(req.getNote());

        pcAssetMapper.insert(asset);
        log.info("PC資産登録: assetNumber={}", asset.getAssetNumber());
        return pcAssetMapper.findById(asset.getId());
    }

    /**
     * PC資産更新
     */
    @Transactional
    public AssetResponse update(Long id, AssetUpdateRequest req) {
        PcAsset existing = pcAssetMapper.findRawById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + id);
        }

        existing.setDeviceName(req.getDeviceName());
        existing.setAcquisitionType(req.getAcquisitionType());
        existing.setMaker(req.getMaker());
        existing.setModelNumber(req.getModelNumber());
        existing.setSerialNumber(req.getSerialNumber());
        existing.setLocation(req.getLocation());
        existing.setStatus(req.getStatus());
        existing.setAssignedEmployeeId(req.getAssignedEmployeeId());
        existing.setHostname(req.getHostname());
        existing.setNote(req.getNote());

        pcAssetMapper.update(existing);
        log.info("PC資産更新: id={}", id);
        return pcAssetMapper.findById(id);
    }

    /**
     * PC資産削除（論理削除ではなく物理削除 - 廃棄フロー想定）
     */
    @Transactional
    public void delete(Long id) {
        PcAsset existing = pcAssetMapper.findRawById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + id);
        }
        pcAssetMapper.deleteById(id);
        log.info("PC資産削除: id={}", id);
    }
}
