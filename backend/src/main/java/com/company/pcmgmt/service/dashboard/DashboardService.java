package com.company.pcmgmt.service.dashboard;

import com.company.pcmgmt.api.dto.response.dashboard.DashboardStatsResponse;
import com.company.pcmgmt.domain.mapper.loan.LoanMapper;
import com.company.pcmgmt.domain.mapper.asset.PcAssetMapper;
import com.company.pcmgmt.domain.mapper.rental.RentalMapper;
import com.company.pcmgmt.domain.mapper.software.SoftwareMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * ダッシュボード統計サービス
 *
 * <p>PC管理システムのダッシュボード画面に表示するシステム全体の集計サマリを提供する。
 * 各マッパーから件数を取得して {@link DashboardStatsResponse} に組み立てて返す。</p>
 */
@Service
@RequiredArgsConstructor
public class DashboardService {

    /** PC資産の統計取得に使用するマッパー */
    private final PcAssetMapper pcAssetMapper;

    /** 貸出統計の取得に使用するマッパー */
    private final LoanMapper loanMapper;

    /** レンタル統計の取得に使用するマッパー */
    private final RentalMapper rentalMapper;

    /** ソフトウェアライセンス統計の取得に使用するマッパー */
    private final SoftwareMapper softwareMapper;

    /**
     * ダッシュボード表示用の統計情報を取得する
     *
     * <p>各マッパーから以下の集計値を取得して返す：
     * <ul>
     *   <li>PC資産総台数</li>
     *   <li>使用中PC台数</li>
     *   <li>保管中PC台数</li>
     *   <li>貸出中件数</li>
     *   <li>レンタル期限切れ間近の件数（90日以内）</li>
     *   <li>レンタル期限切れ済みの件数</li>
     *   <li>ライセンス超過ソフトウェア件数</li>
     * </ul>
     * </p>
     *
     * @return ダッシュボード統計レスポンス
     */
    @Transactional(readOnly = true)
    public DashboardStatsResponse getStats() {
        return DashboardStatsResponse.builder()
                // PC資産総台数
                .totalPcCount(pcAssetMapper.countTotal())
                // 使用中（IN_USE）のPC台数
                .inUsePcCount(pcAssetMapper.countByStatus("IN_USE"))
                // 保管中（IN_STORAGE）のPC台数
                .inStoragePcCount(pcAssetMapper.countByStatus("IN_STORAGE"))
                // 返却されていない（貸出中）件数
                .activeLoansCount(loanMapper.countActive())
                // 90日以内に終了する（未返却）レンタル契約件数
                .nearExpiryRentalsCount(rentalMapper.countNearExpiry())
                // 終了日を超過している（未返却）レンタル契約件数
                .expiredRentalsCount(rentalMapper.countExpired())
                // インストール数が購入ライセンス数を超えているソフトウェア件数
                .licenseOverCount(softwareMapper.countOverLimit())
                .build();
    }
}
