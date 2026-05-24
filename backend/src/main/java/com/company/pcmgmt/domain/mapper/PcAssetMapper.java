package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.api.dto.request.AssetSearchRequest;
import com.company.pcmgmt.api.dto.response.AssetResponse;
import com.company.pcmgmt.domain.entity.PcAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PcAssetMapper {

    List<AssetResponse> findAll(@Param("req") AssetSearchRequest req);

    long countAll(@Param("req") AssetSearchRequest req);

    AssetResponse findById(@Param("id") Long id);

    PcAsset findRawById(@Param("id") Long id);

    int insert(PcAsset asset);

    int update(PcAsset asset);

    int deleteById(@Param("id") Long id);

    boolean existsByAssetNumber(@Param("assetNumber") String assetNumber,
                                @Param("excludeId") Long excludeId);

    // ダッシュボード統計
    long countByStatus(@Param("status") String status);

    long countTotal();
}
