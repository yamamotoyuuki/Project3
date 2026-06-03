package com.company.pcmgmt.service.common;

import com.company.pcmgmt.api.dto.response.common.CodeValueResponse;
import com.company.pcmgmt.domain.mapper.common.CodeMasterMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * コードマスタサービス
 *
 * <p>{@code code_master} テーブルからコード値を取得するビジネスロジック層。
 * フロントエンドのドロップダウンリスト表示（ステータス・機器種別・取得区分など）に使用する。</p>
 *
 * <p>全操作は読み取り専用（{@code @Transactional(readOnly = true)}）。</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeMasterService {

    /** コードマスタの DB アクセスを担うマッパー */
    private final CodeMasterMapper codeMasterMapper;

    /**
     * 指定したコード区分の有効なコード値を表示順（sort_order 昇順）で取得する。
     *
     * <p>{@code is_active = 1} のレコードのみを返す。
     * 結果は {@code code_master} に定義された {@code sort_order ASC} で並べ替えられる。</p>
     *
     * <p>使用例: codeType = "PC_STATUS" の場合、以下を返す。
     * [{codeValue:"IN_STORAGE", codeLabel:"保管中"},
     *  {codeValue:"IN_USE",     codeLabel:"使用中"},
     *  {codeValue:"DISPOSED",   codeLabel:"廃棄済み"}]</p>
     *
     * @param codeType コード区分キー（例: "PC_STATUS", "DEVICE_TYPE", "ACQUISITION_TYPE"）
     * @return コード値と表示ラベルのペアのリスト。該当するコード区分が存在しない場合は空リストを返す。
     */
    @Transactional(readOnly = true)
    public List<CodeValueResponse> findActiveByCodeType(String codeType) {
        log.debug("コード値取得開始: codeType={}", codeType);
        // 有効なコード値を sort_order 昇順で取得
        List<CodeValueResponse> result = codeMasterMapper.findActiveByCodeType(codeType);
        log.debug("コード値取得完了: codeType={}, 件数={}", codeType, result.size());
        return result;
    }
}
