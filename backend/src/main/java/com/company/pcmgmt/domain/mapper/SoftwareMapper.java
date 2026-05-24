package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.api.dto.request.SoftwareSearchRequest;
import com.company.pcmgmt.api.dto.response.SoftwareResponse;
import com.company.pcmgmt.domain.entity.SoftwareMaster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ソフトウェアマスタ MyBatis マッパーインターフェース
 *
 * <p>software_master テーブルに対する CRUD および集計クエリを定義する。
 * インストール数の集計には pc_software_info テーブルとの JOIN を行う。
 * 実装は {@code resources/mapper/SoftwareMapper.xml} に記述する。</p>
 */
@Mapper
public interface SoftwareMapper {

    /**
     * 検索条件・ページネーションを適用してソフトウェア一覧を取得する
     *
     * <p>pc_software_info テーブルとの LEFT JOIN でインストール台数も取得する。</p>
     *
     * @param req 検索条件（キーワード・超過フィルター・ページ情報）
     * @return SoftwareResponse のリスト（installedCount 含む）
     */
    List<SoftwareResponse> findAll(@Param("req") SoftwareSearchRequest req);

    /**
     * 検索条件に一致する総件数を取得する（ページネーション用）
     *
     * @param req 検索条件
     * @return 総件数
     */
    long countAll(@Param("req") SoftwareSearchRequest req);

    /**
     * 指定IDのソフトウェアを取得する（レスポンス形式、インストール数含む）
     *
     * @param id ソフトウェアマスタID
     * @return SoftwareResponse（存在しない場合は null）
     */
    SoftwareResponse findById(@Param("id") Long id);

    /**
     * 指定IDのソフトウェアをエンティティ形式で取得する（更新処理用）
     *
     * @param id ソフトウェアマスタID
     * @return SoftwareMaster エンティティ（存在しない場合は null）
     */
    SoftwareMaster findRawById(@Param("id") Long id);

    /**
     * ソフトウェアを新規登録する
     *
     * <p>INSERT 後、自動採番された ID が {@code software.id} にセットされる。</p>
     *
     * @param software 登録するソフトウェアエンティティ
     * @return 挿入件数（通常 1）
     */
    int insert(SoftwareMaster software);

    /**
     * ソフトウェア情報を更新する
     *
     * @param software 更新するソフトウェアエンティティ（id フィールドが必須）
     * @return 更新件数（通常 1）
     */
    int update(SoftwareMaster software);

    /**
     * ライセンス超過件数を取得する（ダッシュボード統計用）
     *
     * <p>インストール台数が購入ライセンス数を超えているソフトウェアの件数。
     * 購入ライセンス数が 0 のものは対象外。</p>
     *
     * @return ライセンス超過ソフトウェア件数
     */
    long countOverLimit();
}
