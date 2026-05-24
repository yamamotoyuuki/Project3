package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.LoanCreateRequest;
import com.company.pcmgmt.api.dto.request.LoanReturnRequest;
import com.company.pcmgmt.api.dto.request.LoanSearchRequest;
import com.company.pcmgmt.api.dto.response.LoanResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.domain.entity.PcLoan;
import com.company.pcmgmt.domain.mapper.LoanMapper;
import com.company.pcmgmt.domain.mapper.PcAssetMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import com.company.pcmgmt.security.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanMapper loanMapper;
    private final PcAssetMapper pcAssetMapper;
    private final UserDetailsServiceImpl userDetailsService;

    @Transactional(readOnly = true)
    public PageResponse<LoanResponse> findAll(LoanSearchRequest req) {
        List<LoanResponse> content = loanMapper.findAll(req);
        long total = loanMapper.countAll(req);
        return PageResponse.of(content, total, req.getPage(), req.getSize());
    }

    @Transactional(readOnly = true)
    public LoanResponse findById(Long id) {
        LoanResponse loan = loanMapper.findById(id);
        if (loan == null) {
            throw new ResourceNotFoundException("貸出記録が見つかりません: id=" + id);
        }
        return loan;
    }

    @Transactional
    public LoanResponse create(LoanCreateRequest req) {
        // 対象PCが存在するか確認
        var asset = pcAssetMapper.findRawById(req.getPcAssetId());
        if (asset == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + req.getPcAssetId());
        }

        // すでに貸出中でないか確認
        LoanResponse activeLoan = loanMapper.findActiveLoanByAssetId(req.getPcAssetId());
        if (activeLoan != null) {
            throw new IllegalStateException(
                "このPCはすでに貸出中です（貸出先: " + activeLoan.getEmployeeName() + "）"
            );
        }

        PcLoan loan = new PcLoan();
        loan.setPcAssetId(req.getPcAssetId());
        loan.setEmployeeId(req.getEmployeeId());
        loan.setLoanDate(req.getLoanDate());
        loan.setExpectedReturnDate(req.getExpectedReturnDate());
        loan.setPurpose(req.getPurpose());
        loan.setNote(req.getNote());
        loan.setCreatedBy(getCurrentUserId());

        loanMapper.insert(loan);
        log.info("貸出登録: loanId={}, assetId={}", loan.getId(), loan.getPcAssetId());
        return loanMapper.findById(loan.getId());
    }

    @Transactional
    public LoanResponse returnLoan(Long id, LoanReturnRequest req) {
        PcLoan loan = loanMapper.findRawById(id);
        if (loan == null) {
            throw new ResourceNotFoundException("貸出記録が見つかりません: id=" + id);
        }
        if (loan.getActualReturnDate() != null) {
            throw new IllegalStateException("この貸出はすでに返却済みです");
        }

        loan.setActualReturnDate(req.getActualReturnDate());
        if (req.getNote() != null) {
            loan.setNote(req.getNote());
        }
        loanMapper.update(loan);
        log.info("返却登録: loanId={}", id);
        return loanMapper.findById(id);
    }

    /** ログインユーザーの ID を取得 */
    private Long getCurrentUserId() {
        try {
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return 1L;
            UserDetails ud = (UserDetails) auth.getPrincipal();
            var user = userDetailsService.loadUserEntityByUsername(ud.getUsername());
            return user != null ? user.getId() : 1L;
        } catch (Exception e) {
            return 1L;
        }
    }
}
