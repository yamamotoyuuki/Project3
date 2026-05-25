package com.company.pcmgmt.service;

import com.company.pcmgmt.domain.entity.OperationLog;
import com.company.pcmgmt.domain.mapper.OperationLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 操作ログ記録サービス
 *
 * <p>操作ログを {@code operation_logs} テーブルへ非同期で書き込む。
 * ログ書き込みの失敗が本来の業務処理を妨げないよう、以下の設計を採用する:</p>
 * <ul>
 *   <li>{@link Async}: 別スレッドで非同期実行（レスポンス遅延を防ぐ）</li>
 *   <li>{@code Propagation.REQUIRES_NEW}: 業務トランザクションとは独立した
 *       新規トランザクションで実行（業務のロールバックに影響されない）</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OperationLogService {

    /** 操作ログの DB アクセスを担うマッパー */
    private final OperationLogMapper operationLogMapper;

    /**
     * 操作ログを非同期で記録する
     *
     * <p>例外が発生してもスタックトレースをログに出力するのみで、
     * 呼び出し元（AOP アスペクト）には例外を伝播させない。</p>
     *
     * @param entry 記録するログエンティティ（変数名 "log" は Lombok @Slf4j フィールドと衝突するため "entry" を使用）
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void record(OperationLog entry) {
        try {
            operationLogMapper.insert(entry);
        } catch (Exception e) {
            // ログ記録の失敗は業務処理に影響させない（エラーログのみ出力）
            log.error("操作ログの記録に失敗しました: {}", e.getMessage(), e);
        }
    }
}
