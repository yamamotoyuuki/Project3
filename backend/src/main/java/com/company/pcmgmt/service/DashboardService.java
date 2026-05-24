package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.response.DashboardStatsResponse;
import com.company.pcmgmt.domain.mapper.PcAssetMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PcAssetMapper pcAssetMapper;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        return DashboardStatsResponse.builder()
                .totalPcCount(pcAssetMapper.countTotal())
                .inUsePcCount(pcAssetMapper.countByStatus("IN_USE"))
                .inStoragePcCount(pcAssetMapper.countByStatus("IN_STORAGE"))
                // Phase 3 で loans / rentals mapper 追加後に実装
                .activeLoansCount(0)
                .nearExpiryRentalsCount(0)
                .expiredRentalsCount(0)
                .licenseOverCount(0)
                .build();
    }
}
