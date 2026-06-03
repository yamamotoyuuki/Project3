package com.company.pcmgmt.domain.mapper.asset;

import com.company.pcmgmt.api.dto.request.asset.AssetSearchRequest;
import com.company.pcmgmt.api.dto.response.asset.AssetResponse;
import com.company.pcmgmt.domain.entity.PcAsset;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * PC資産 MyBatis マッパーインターフェース
 *
 * <p>pc_assets テーブルに対する CRUD および集計クエリを定義する。
 * 実装は {@code resources/mapper/PcAssetMapper.xml} に記述する。</p>
 */
@Mapper
public interface PcAssetMapper {

    /**
     * 検索条件・ページネーションを適用してPC資産一覧を取得する
     *
     * <p>社員名（assignedEmployeeName）は employees テーブルとの LEFT JOIN で取得する。</p>
     *
     * @param req 検索条件（キーワード・ステータス・取得区分・場所・ページ情報）
     * @return AssetResponse のリスト（担当社員名含む）
     */
    List<AssetResponse> findAll(@Param("req") AssetSearchRequest req);

    /**
     * 検索条件に一致する総件数を取得する（ページネーション用）
     *
     * @param req 検索条件
     * @return 総件数
     */
    long countAll(@Param("req") AssetSearchRequest req);

    /**
     * 指定IDのPC資産を取得する（レスポンス形式、担当社員名含む）
     *
     * @param id PC資産ID
     * @return AssetResponse（存在しない場合は null）
     */
    AssetResponse findById(@Param("id") Long id);

    /**
     * 指定IDのPC資産をエンティティ形式で取得する（更新処理用）
     *
     * @param id PC資産ID
     * @return PcAsset エンティティ（存在しない場合は null）
     */
    PcAsset findRawById(@Param("id") Long id);

    /**
     * PC資産を新規登録する
     *
     * <p>INSERT 後、自動採番された ID が {@code asset.id} にセットされる。</p>
     *
     * @param asset 登録するPC資産エンティティ
     * @return 挿入件数（通常 1）
     */
    int insert(PcAsset asset);

    /**
     * PC資産を更新する
     *
     * @param asset 更新するPC資産エンティティ（id フィールドが必須）
     * @return 更新件数（通常 1）
     */
    int update(PcAsset asset);

    /**
     * 指定IDのPC資産を物理削除する
     *
     * @param id 削除するPC資産のID
     * @return 削除件数（通常 1）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 資産番号の重複チェックを行う
     *
     * @param assetNumber チェック対象の資産番号
     * @param excludeId   更新時に自分自身を除外するためのID（新規登録時は null）
     * @return 重複している場合は true
     */
    boolean existsByAssetNumber(@Param("assetNumber") String assetNumber,
                                @Param("excludeId") Long excludeId);

    /**
     * 指定ステータスのPC資産台数を取得する（ダッシュボード統計用）
     *
     * @param status 対象ステータス（例: "IN_USE", "IN_STORAGE"）
     * @return 台数
     */
    long countByStatus(@Param("status") String status);

    /**
     * PC資産の総台数を取得する（ダッシュボード統計用）
     *
     * @return PC資産総台数
     */
    long countTotal();
}
