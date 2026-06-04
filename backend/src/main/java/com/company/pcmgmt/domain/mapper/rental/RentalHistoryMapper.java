package com.company.pcmgmt.domain.mapper.rental;

import com.company.pcmgmt.api.dto.response.rental.RentalHistoryResponse;
import com.company.pcmgmt.domain.entity.RentalHistory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * レンタル契約変更履歴マッパー
 *
 * <p>{@code pc_rental_history} テーブルへのデータアクセスを提供する。</p>
 */
@Mapper
public interface RentalHistoryMapper {

    /**
     * 指定レンタル契約の変更履歴一覧を取得する。
     * 変更日時の降順（新しい順）で返す。
     *
     * @param rentalId レンタル契約ID
     * @return 変更履歴レスポンスのリスト
     */
    List<RentalHistoryResponse> findByRentalId(@Param("rentalId") Long rentalId);

    /**
     * 変更履歴を1件登録する。
     * 1操作で複数フィールドが変わる場合はこのメソッドを複数回呼び出す。
     *
     * @param history 登録する変更履歴エンティティ
     */
    void insert(RentalHistory history);
}
