package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.api.dto.request.RentalSearchRequest;
import com.company.pcmgmt.api.dto.response.RentalResponse;
import com.company.pcmgmt.api.dto.response.RentalVendorResponse;
import com.company.pcmgmt.domain.entity.PcAcquisitionRental;
import com.company.pcmgmt.domain.entity.RentalVendor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * レンタル契約・ベンダー MyBatis マッパーインターフェース
 *
 * <p>pc_acquisition_rentals テーブルと rental_vendors テーブルに対する
 * CRUD および集計クエリを定義する。
 * 実装は {@code resources/mapper/RentalMapper.xml} に記述する。</p>
 */
@Mapper
public interface RentalMapper {

    // ======= レンタル契約 =======

    /**
     * 検索条件・ページネーションを適用してレンタル契約一覧を取得する
     *
     * <p>PC資産情報・ベンダー情報を LEFT JOIN で取得する。</p>
     *
     * @param req 検索条件（キーワード・期限フィルター・返却状況・ページ情報）
     * @return RentalResponse のリスト
     */
    List<RentalResponse> findAll(@Param("req") RentalSearchRequest req);

    /**
     * 検索条件に一致する総件数を取得する（ページネーション用）
     *
     * @param req 検索条件
     * @return 総件数
     */
    long countAll(@Param("req") RentalSearchRequest req);

    /**
     * 指定IDのレンタル契約を取得する（レスポンス形式）
     *
     * @param id レンタル契約ID
     * @return RentalResponse（存在しない場合は null）
     */
    RentalResponse findById(@Param("id") Long id);

    /**
     * 指定IDのレンタル契約をエンティティ形式で取得する（更新処理用）
     *
     * @param id レンタル契約ID
     * @return PcAcquisitionRental エンティティ（存在しない場合は null）
     */
    PcAcquisitionRental findRawById(@Param("id") Long id);

    /**
     * レンタル契約を新規登録する
     *
     * <p>INSERT 後、自動採番された ID が {@code rental.id} にセットされる。</p>
     *
     * @param rental 登録するレンタル契約エンティティ
     * @return 挿入件数（通常 1）
     */
    int insert(PcAcquisitionRental rental);

    /**
     * レンタル契約を更新する（主に返却日の登録に使用）
     *
     * @param rental 更新するレンタル契約エンティティ（id フィールドが必須）
     * @return 更新件数（通常 1）
     */
    int update(PcAcquisitionRental rental);

    /**
     * 90日以内に終了日を迎える（未返却の）レンタル契約件数を取得する（ダッシュボード統計用）
     *
     * @return 90日以内に期限切れになるレンタル契約件数
     */
    long countNearExpiry();

    /**
     * 終了日を超過している（未返却の）レンタル契約件数を取得する（ダッシュボード統計用）
     *
     * @return 期限切れのレンタル契約件数
     */
    long countExpired();

    // ======= ベンダー =======

    /**
     * 全レンタルベンダーを取得する
     *
     * @return RentalVendorResponse のリスト
     */
    List<RentalVendorResponse> findAllVendors();

    /**
     * 指定IDのベンダーを取得する（レスポンス形式）
     *
     * @param id ベンダーID
     * @return RentalVendorResponse（存在しない場合は null）
     */
    RentalVendorResponse findVendorById(@Param("id") Long id);

    /**
     * 指定IDのベンダーをエンティティ形式で取得する（更新処理用）
     *
     * @param id ベンダーID
     * @return RentalVendor エンティティ（存在しない場合は null）
     */
    RentalVendor findRawVendorById(@Param("id") Long id);

    /**
     * ベンダーを新規登録する
     *
     * <p>INSERT 後、自動採番された ID が {@code vendor.id} にセットされる。</p>
     *
     * @param vendor 登録するベンダーエンティティ
     * @return 挿入件数（通常 1）
     */
    int insertVendor(RentalVendor vendor);

    /**
     * ベンダー情報を更新する
     *
     * @param vendor 更新するベンダーエンティティ（id フィールドが必須）
     * @return 更新件数（通常 1）
     */
    int updateVendor(RentalVendor vendor);
}
