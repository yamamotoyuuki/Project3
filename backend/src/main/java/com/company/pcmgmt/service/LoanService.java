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

/**
 * PC貸出管理サービス
 *
 * <p>PC貸出の登録・返却・一覧取得などのビジネスロジックを担当する。
 * 貸出登録時には二重貸出チェック・PC資産存在確認を行う。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoanService {

    /** 貸出記録の DB アクセスを担うマッパー */
    private final LoanMapper loanMapper;

    /** PC資産の存在確認に使用するマッパー */
    private final PcAssetMapper pcAssetMapper;

    /** ログインユーザーID取得に使用するセキュリティサービス */
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * 貸出一覧をページネーション・絞り込み条件付きで取得する
     *
     * @param req 検索条件（キーワード・返却状況・ページ情報）
     * @return ページネーション付き貸出レスポンス
     */
    @Transactional(readOnly = true)
    public PageResponse<LoanResponse> findAll(LoanSearchRequest req) {
        // 検索条件に一致する貸出リストを取得
        List<LoanResponse> content = loanMapper.findAll(req);
        // 総件数を取得（ページネーション計算用）
        long total = loanMapper.countAll(req);
        return PageResponse.of(content, total, req.getPage(), req.getSize());
    }

    /**
     * 指定IDの貸出記録詳細を取得する
     *
     * @param id 貸出記録ID
     * @return LoanResponse
     * @throws ResourceNotFoundException 指定IDの貸出記録が存在しない場合
     */
    @Transactional(readOnly = true)
    public LoanResponse findById(Long id) {
        LoanResponse loan = loanMapper.findById(id);
        // 存在しない場合は 404 例外をスロー
        if (loan == null) {
            throw new ResourceNotFoundException("貸出記録が見つかりません: id=" + id);
        }
        return loan;
    }

    /**
     * PC貸出を登録する
     *
     * <p>以下の事前チェックを行う。
     * <ol>
     *   <li>対象PC資産の存在確認</li>
     *   <li>対象PCが既に貸出中でないか確認（二重貸出防止）</li>
     * </ol>
     * </p>
     *
     * @param req 貸出登録リクエスト
     * @return 登録後の貸出レスポンス
     * @throws ResourceNotFoundException 対象PC資産が存在しない場合
     * @throws IllegalStateException     対象PCが既に貸出中の場合
     */
    @Transactional
    public LoanResponse create(LoanCreateRequest req) {
        // 対象PCが存在するか確認
        var asset = pcAssetMapper.findRawById(req.getPcAssetId());
        if (asset == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + req.getPcAssetId());
        }

        // すでに貸出中でないか確認（actualReturnDate IS NULL のレコードが存在する場合は貸出中）
        LoanResponse activeLoan = loanMapper.findActiveLoanByAssetId(req.getPcAssetId());
        if (activeLoan != null) {
            throw new IllegalStateException(
                "このPCはすでに貸出中です（貸出先: " + activeLoan.getEmployeeName() + "）"
            );
        }

        // リクエストからエンティティを組み立て
        PcLoan loan = new PcLoan();
        loan.setPcAssetId(req.getPcAssetId());                      // 貸出PC資産ID
        loan.setEmployeeId(req.getEmployeeId());                    // 貸出先社員ID
        loan.setLoanDate(req.getLoanDate());                        // 貸出日
        loan.setExpectedReturnDate(req.getExpectedReturnDate());    // 返却予定日
        loan.setPurpose(req.getPurpose());                          // 貸出目的
        loan.setNote(req.getNote());                               // 備考
        // ログインユーザーのIDを登録者としてセット
        loan.setCreatedBy(getCurrentUserId());

        // DBに保存（INSERT 後、loan.id に自動採番IDがセットされる）
        loanMapper.insert(loan);
        log.info("貸出登録: loanId={}, assetId={}", loan.getId(), loan.getPcAssetId());

        // 保存後のデータ（JOIN情報を含む）を返す
        return loanMapper.findById(loan.getId());
    }

    /**
     * PC返却を登録する
     *
     * @param id  返却対象の貸出記録ID
     * @param req 返却登録リクエスト
     * @return 更新後の貸出レスポンス
     * @throws ResourceNotFoundException 指定IDの貸出記録が存在しない場合
     * @throws IllegalStateException     既に返却済みの場合
     */
    @Transactional
    public LoanResponse returnLoan(Long id, LoanReturnRequest req) {
        // 返却対象の貸出記録を取得
        PcLoan loan = loanMapper.findRawById(id);
        if (loan == null) {
            throw new ResourceNotFoundException("貸出記録が見つかりません: id=" + id);
        }
        // 既に返却済みの場合はエラー
        if (loan.getActualReturnDate() != null) {
            throw new IllegalStateException("この貸出はすでに返却済みです");
        }

        // 実際の返却日をセット
        loan.setActualReturnDate(req.getActualReturnDate());
        // 備考が指定されていれば更新
        if (req.getNote() != null) {
            loan.setNote(req.getNote());
        }
        loanMapper.update(loan);
        log.info("返却登録: loanId={}", id);

        // 更新後のデータを返す
        return loanMapper.findById(id);
    }

    /**
     * ログインユーザーの ID を取得する（内部処理用）
     *
     * <p>SecurityContextHolder からユーザー情報を取得する。
     * 取得できない場合はフォールバック値として 1L を返す。</p>
     *
     * @return ログインユーザーのID（取得できない場合は 1L）
     */
    private Long getCurrentUserId() {
        try {
            // Spring Security のコンテキストから認証情報を取得
            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null) return 1L;
            // Principal（ユーザー詳細）からユーザー名を取得
            UserDetails ud = (UserDetails) auth.getPrincipal();
            // ユーザー名からDBのエンティティを取得してIDを返す
            var user = userDetailsService.loadUserEntityByUsername(ud.getUsername());
            return user != null ? user.getId() : 1L;
        } catch (Exception e) {
            // 取得に失敗した場合はデフォルト値を返す
            return 1L;
        }
    }
}
