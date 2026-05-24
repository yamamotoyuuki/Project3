package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.response.DashboardStatsResponse;
import com.company.pcmgmt.domain.mapper.LoanMapper;
import com.company.pcmgmt.domain.mapper.PcAssetMapper;
import com.company.pcmgmt.domain.mapper.RentalMapper;
import com.company.pcmgmt.domain.mapper.SoftwareMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PcAssetMapper pcAssetMapper;
    private final LoanMapper loanMapper;
    private final RentalMapper rentalMapper;
    private final SoftwareMapper softwareMapper;

    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        return DashboardStatsResponse.builder()
                .totalPcCount(pcAssetMapper.countTotal())
                .inUsePcCount(pcAssetMapper.countByStatus("IN_USE"))
                .inStoragePcCount(pcAssetMapper.countByStatus("IN_STORAGE"))
                .activeLoansCount(loanMapper.countActive())
                .nearExpiryRentalsCount(rentalMapper.countNearExpiry())
                .expiredRentalsCount(rentalMapper.countExpired())
                .licenseOverCount(softwareMapper.countOverLimit())
                .build();
    }
}
