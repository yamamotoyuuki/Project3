package com.company.pcmgmt.domain.mapper.common;

import com.company.pcmgmt.api.dto.response.common.CodeValueResponse;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * コードマスタ MyBatis マッパーインターフェース
 *
 * <p>{@code code_master} テーブルへの読み取り専用アクセスを提供する。
 * SQL は {@code resources/com/company/pcmgmt/domain/mapper/common/CodeMasterMapper.xml} に定義する。</p>
 *
 * <p>用途: ドロップダウンリスト表示用のコード値をコード区分キーで取得する
 * （例: PCステータス一覧、機器種別一覧）。</p>
 */
@Mapper
public interface CodeMasterMapper {

    /**
     * 指定したコード区分の有効なコード値を表示順（sort_order 昇順）で取得する。
     *
     * <p>SQL 絞り込み条件:
     * <ul>
     *   <li>{@code code_type = codeType} -- コード区分キーで絞り込み</li>
     *   <li>{@code is_active = 1} -- 有効なレコードのみ取得（無効コードは除外）</li>
     *   <li>{@code ORDER BY sort_order ASC} -- 画面表示順に並べ替え</li>
     * </ul>
     * </p>
     *
     * @param codeType コード区分キー（例: "PC_STATUS", "DEVICE_TYPE", "ACQUISITION_TYPE"）
     * @return {@link CodeValueResponse} のリスト（code_value と code_label のペア）。
     *         対象レコードが存在しない場合は空リストを返す。
     */
    List<CodeValueResponse> findActiveByCodeType(@Param("codeType") String codeType);
}
