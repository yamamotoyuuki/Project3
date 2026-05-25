package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.domain.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 操作ログ MyBatis マッパーインターフェース
 *
 * <p>{@code operation_logs} テーブルへのデータアクセスを担う。
 * SQL は {@code OperationLogMapper.xml} で管理する。</p>
 */
@Mapper
public interface OperationLogMapper {

    /**
     * 操作ログを1件挿入する
     *
     * @param log 挿入する操作ログエンティティ
     */
    void insert(OperationLog log);
}
