package com.company.pcmgmt.domain.mapper.agent;

import com.company.pcmgmt.domain.entity.AgentEnrollmentToken;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * エージェント登録トークン MyBatis マッパーインターフェース
 *
 * <p>agent_enrollment_tokens テーブルの操作を定義する。
 * 実装は {@code AgentEnrollmentTokenMapper.xml} に記述する。</p>
 */
@Mapper
public interface AgentEnrollmentTokenMapper {

    /**
     * 登録トークンを新規発行して INSERT する
     *
     * @param token 発行するトークンエンティティ（token, expiresAt, note, createdByUserId を設定済み）
     * @return 挿入件数（通常 1）
     */
    int insert(AgentEnrollmentToken token);

    /**
     * トークン文字列でトークンを検索する（登録時の検証に使用）
     *
     * <p>検証ロジックはサービス層で行う。このメソッドは存在有無の確認のみ。</p>
     *
     * @param token 検索するトークン文字列
     * @return AgentEnrollmentToken エンティティ（存在しない場合は null）
     */
    AgentEnrollmentToken findByToken(@Param("token") String token);

    /**
     * トークンを使用済みにマークする（1回限りの制御）
     *
     * <p>エージェントが登録に成功したとき、used_at と used_by_agent_number を設定する。</p>
     *
     * @param token              使用済みにするトークン文字列
     * @param usedByAgentNumber  このトークンで登録したエージェント番号
     * @return 更新件数（通常 1）
     */
    int markAsUsed(@Param("token") String token,
                   @Param("usedByAgentNumber") String usedByAgentNumber);

    /**
     * 全トークンを発行日時の降順で取得する（管理画面の一覧表示に使用）
     *
     * @return トークン一覧（新しい順）
     */
    List<AgentEnrollmentToken> findAll();

    /**
     * IDでトークンを削除する（管理者による強制無効化）
     *
     * <p>未使用トークンをIT担当者がキャンセルする際に使用する。</p>
     *
     * @param id 削除するトークンのID
     * @return 削除件数（通常 1）
     */
    int deleteById(@Param("id") Long id);
}
