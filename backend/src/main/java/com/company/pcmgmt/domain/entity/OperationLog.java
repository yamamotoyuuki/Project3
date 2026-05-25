package com.company.pcmgmt.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 操作ログエンティティ
 *
 * <p>{@code operation_logs} テーブルに対応するエンティティクラス。
 * システム上の操作（作成・更新・削除等）を記録する監査ログとして使用する。</p>
 *
 * <p>レコードは {@link com.company.pcmgmt.aop.OperationLoggingAspect} によって
 * {@link com.company.pcmgmt.annotation.Loggable} アノテーション付きメソッドの実行後に
 * 自動挿入される。</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperationLog {

    /** 主キー（AUTO_INCREMENT） */
    private Long id;

    /** 操作を実行したユーザーID（未認証操作の場合は null） */
    private Long userId;

    /** 操作を実行したユーザー名（ログ可読性のため非正規化して保持） */
    private String username;

    /** 操作種別（例: "CREATE", "UPDATE", "DELETE", "RETURN", "LOGIN"） */
    private String operation;

    /** 操作対象のリソース種別（例: "PC資産", "社員", "貸出"） */
    private String targetType;

    /** 操作対象のリソースID（該当しない場合は null） */
    private Long targetId;

    /** 操作内容の詳細（JSON 文字列や自由記述） */
    private String detail;

    /** 操作を行ったクライアントの IP アドレス */
    private String ipAddress;

    /** ログ作成日時（DB デフォルト: CURRENT_TIMESTAMP） */
    private LocalDateTime createdAt;
}
