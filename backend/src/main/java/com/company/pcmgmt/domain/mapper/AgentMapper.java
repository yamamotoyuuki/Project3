package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.domain.entity.PcAsset;
import com.company.pcmgmt.domain.entity.PcHardwareInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * エージェント情報収集 MyBatis マッパーインターフェース
 *
 * <p>Tauri エージェントから受信したハードウェア・ソフトウェア・ネットワーク情報を
 * 各テーブルに保存するためのクエリを定義する。
 * 実装は {@code resources/mapper/AgentMapper.xml} に記述する。</p>
 */
@Mapper
public interface AgentMapper {

    /**
     * ホスト名でPC資産を検索する
     *
     * <p>エージェントが報告してきたホスト名を使ってPC資産を特定する。
     * 対応するPC資産が存在しない場合は null を返す。</p>
     *
     * @param hostname エージェントが報告したホスト名
     * @return 対応する PcAsset エンティティ（存在しない場合は null）
     */
    PcAsset findAssetByHostname(@Param("hostname") String hostname);

    // ---- ハードウェア情報 (pc_hardware_info テーブル) ----

    /**
     * 指定PC資産のハードウェア情報を取得する（upsert の存在確認用）
     *
     * @param pcAssetId 対象PC資産のID
     * @return PcHardwareInfo エンティティ（未登録の場合は null）
     */
    PcHardwareInfo findHardwareByAssetId(@Param("pcAssetId") Long pcAssetId);

    /**
     * ハードウェア情報を新規登録する（初回報告時）
     *
     * @param info 登録するハードウェア情報エンティティ
     * @return 挿入件数（通常 1）
     */
    int insertHardware(PcHardwareInfo info);

    /**
     * ハードウェア情報を更新する（2回目以降の報告時）
     *
     * @param info 更新するハードウェア情報エンティティ（pcAssetId フィールドが必須）
     * @return 更新件数（通常 1）
     */
    int updateHardware(PcHardwareInfo info);

    // ---- ソフトウェア情報 (pc_software_info テーブル) ----

    /**
     * 指定PC資産のソフトウェア情報を全件削除する（再登録前の全削除）
     *
     * <p>毎回の報告でソフトウェアリストを完全に置き換えるため、
     * 削除→再INSERTの流れで更新する。</p>
     *
     * @param pcAssetId 対象PC資産のID
     * @return 削除件数
     */
    int deleteSoftwareByAssetId(@Param("pcAssetId") Long pcAssetId);

    /**
     * ソフトウェア情報を1件登録する
     *
     * @param pcAssetId    対象PC資産のID
     * @param softwareName ソフトウェア名
     * @param version      バージョン
     * @param publisher    発行元
     * @param installDate  インストール日
     * @return 挿入件数（通常 1）
     */
    int insertSoftware(@Param("pcAssetId") Long pcAssetId,
                       @Param("softwareName") String softwareName,
                       @Param("version") String version,
                       @Param("publisher") String publisher,
                       @Param("installDate") String installDate);

    // ---- ネットワーク情報 (pc_network_info テーブル) ----

    /**
     * 指定PC資産のネットワーク情報を全件削除する（再登録前の全削除）
     *
     * <p>毎回の報告でネットワーク情報を完全に置き換えるため、
     * 削除→再INSERTの流れで更新する。</p>
     *
     * @param pcAssetId 対象PC資産のID
     * @return 削除件数
     */
    int deleteNetworkByAssetId(@Param("pcAssetId") Long pcAssetId);

    /**
     * ネットワーク情報（NIC 1枚分）を登録する
     *
     * @param pcAssetId  対象PC資産のID
     * @param nicName    NIC 名称（例: "Wi-Fi"）
     * @param ipAddress  IP アドレス（例: "192.168.1.10"）
     * @param macAddress MAC アドレス（例: "AA:BB:CC:DD:EE:FF"）
     * @return 挿入件数（通常 1）
     */
    int insertNetwork(@Param("pcAssetId") Long pcAssetId,
                      @Param("nicName") String nicName,
                      @Param("ipAddress") String ipAddress,
                      @Param("macAddress") String macAddress);

    // ---- エージェント最終接続日時 (pc_assets テーブルの agent_last_seen カラム) ----

    /**
     * エージェントの最終接続日時を現在日時に更新する
     *
     * <p>エージェントからの報告を受信するたびに呼び出され、
     * PC資産の {@code agentLastSeen} フィールドを更新する。</p>
     *
     * @param pcAssetId 対象PC資産のID
     * @return 更新件数（通常 1）
     */
    int updateAgentLastSeen(@Param("pcAssetId") Long pcAssetId);
}
