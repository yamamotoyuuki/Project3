package com.company.pcmgmt.domain.mapper.loan;

import com.company.pcmgmt.api.dto.request.loan.LoanSearchRequest;
import com.company.pcmgmt.api.dto.response.loan.LoanResponse;
import com.company.pcmgmt.domain.entity.PcLoan;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * PC貸出 MyBatis マッパーインターフェース
 *
 * <p>pc_loans テーブルに対する CRUD および集計クエリを定義する。
 * 実装は {@code resources/mapper/LoanMapper.xml} に記述する。</p>
 */
@Mapper
public interface LoanMapper {

    /**
     * 検索条件・ページネーションを適用して貸出一覧を取得する
     *
     * <p>PC資産情報・社員情報・登録ユーザー情報を LEFT JOIN で取得する。</p>
     *
     * @param req 検索条件（キーワード・返却状況・ページ情報）
     * @return LoanResponse のリスト
     */
    List<LoanResponse> findAll(@Param("req") LoanSearchRequest req);

    /**
     * 検索条件に一致する総件数を取得する（ページネーション用）
     *
     * @param req 検索条件
     * @return 総件数
     */
    long countAll(@Param("req") LoanSearchRequest req);

    /**
     * 指定IDの貸出記録を取得する（レスポンス形式）
     *
     * @param id 貸出記録ID
     * @return LoanResponse（存在しない場合は null）
     */
    LoanResponse findById(@Param("id") Long id);

    /**
     * 指定IDの貸出記録をエンティティ形式で取得する（更新処理用）
     *
     * @param id 貸出記録ID
     * @return PcLoan エンティティ（存在しない場合は null）
     */
    PcLoan findRawById(@Param("id") Long id);

    /**
     * 貸出記録を新規登録する
     *
     * <p>INSERT 後、自動採番された ID が {@code loan.id} にセットされる。</p>
     *
     * @param loan 登録する貸出エンティティ
     * @return 挿入件数（通常 1）
     */
    int insert(PcLoan loan);

    /**
     * 貸出記録を更新する（主に返却日の登録に使用）
     *
     * @param loan 更新する貸出エンティティ（id フィールドが必須）
     * @return 更新件数（通常 1）
     */
    int update(PcLoan loan);

    /**
     * 指定PCの現在の貸出中レコードを取得する
     *
     * <p>{@code actualReturnDate IS NULL} の条件で貸出中レコードを1件取得する。
     * 二重貸出チェックに使用する。</p>
     *
     * @param pcAssetId 対象PC資産のID
     * @return 貸出中の LoanResponse（貸出中でない場合は null）
     */
    LoanResponse findActiveLoanByAssetId(@Param("pcAssetId") Long pcAssetId);

    /**
     * 現在の貸出中件数を取得する（ダッシュボード統計用）
     *
     * @return 返却されていない貸出レコードの総件数
     */
    long countActive();
}
