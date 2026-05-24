package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.api.dto.request.LoanSearchRequest;
import com.company.pcmgmt.api.dto.response.LoanResponse;
import com.company.pcmgmt.domain.entity.PcLoan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LoanMapper {

    List<LoanResponse> findAll(@Param("req") LoanSearchRequest req);

    long countAll(@Param("req") LoanSearchRequest req);

    LoanResponse findById(@Param("id") Long id);

    PcLoan findRawById(@Param("id") Long id);

    int insert(PcLoan loan);

    int update(PcLoan loan);

    /** 指定PCの貸出中レコード（actualReturnDate IS NULL） */
    LoanResponse findActiveLoanByAssetId(@Param("pcAssetId") Long pcAssetId);

    /** ダッシュボード: 貸出中件数 */
    long countActive();
}
