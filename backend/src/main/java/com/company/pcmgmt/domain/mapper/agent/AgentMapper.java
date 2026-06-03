package com.company.pcmgmt.domain.mapper.agent;

import com.company.pcmgmt.domain.entity.Agent;
import com.company.pcmgmt.domain.entity.PcAsset;
import com.company.pcmgmt.domain.entity.PcHardwareInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * エージェント情報収集 MyBatis マッパーインターフェース
 *
 * <p>Tauri エージェントから受信したハードウェア・ソフトウェア・ネットワーク情報を
 * 各テーブルに保存するためのクエリを定義する。
 * 実装は {@code resources/com/company/pcmgmt/domain/mapper/AgentMapper.xml} に記述する。</p>
 *
 * <p>V4 変更: ハードウェア・ソフトウェア・ネットワーク情報のリレーションを
 * {@code pc_assets.id} ベースから {@code agents.agent_number} ベースに変更。</p>
 */
@Mapper
public interface AgentMapper {

    // ---- エージェントマスタ (agents テーブル) ----

    /**
     * エージェント番号でエージェントを検索する
     *
     * <p>受信したリクエストのエージェント番号が登録済みか確認するために使用する。
     * 存在すれば UPDATE、存在しなければ INSERT の分岐（isNew フラグ）を決定する。</p>
     *
     * @param agentNumber エージェント番号（例: "AGT-A1B2C3D4"）
     * @return Agent エンティティ（未登録の場合は null）
     */
    Agent findAgentByNumber(@Param("agentNumber") String agentNumber);

    /**
     * 新規エージェントを登録する（初回起動時 or 初回レポート時）
     *
     * @param agentNumber 生成したエージェント番号
     * @param hostname    エージェントが動作するPCのホスト名
     * @return 挿入件数（通常 1）
     */
    int insertAgent(@Param("agentNumber") String agentNumber,
                    @Param("hostname") String hostname);

    /**
     * エージェントの最終更新日時を更新する（2回目以降のレポート受信時）
     *
     * @param agentNumber 対象エージェント番号
     * @return 更新件数（通常 1）
     */
    int updateAgent(@Param("agentNumber") String agentNumber);

    // ---- PC資産登録・検索 ----

    /**
     * エージェントから新規報告されたPC資産を自動登録する
     *
     * <p>エージェントがレポートを送信した際、ホスト名でもエージェント番号でも
     * 既存の PC 資産が見つからない場合に自動登録する。
     * {@code asset_number} は "AUTO-XXXXXXXX" 形式で自動生成される。</p>
     *
     * @param assetNumber    自動生成された資産番号（例: "AUTO-A1B2C3D4"）
     * @param deviceName     端末名（ホスト名をそのまま使用）
     * @param acquisitionType 取得区分（"PURCHASE" / "RENTAL" / "UNKNOWN"）
     * @param hostname       エージェントが報告したホスト名
     * @param agentNumber    エージェント番号（agents テーブルとの紐付けキー）
     * @param location       設置場所（エージェントが報告した値、null 可）
     * @param userName       使用者名（エージェントが報告した値、null 可）
     * @return 挿入件数（通常 1）
     */
    int insertAsset(@Param("assetNumber")     String assetNumber,
                    @Param("deviceName")      String deviceName,
                    @Param("acquisitionType") String acquisitionType,
                    @Param("hostname")        String hostname,
                    @Param("agentNumber")     String agentNumber,
                    @Param("location")        String location,
                    @Param("userName")        String userName);

    /**
     * エージェント番号でPC資産を検索する（主検索）
     *
     * <p>エージェントが報告してきたエージェント番号を使ってPC資産を特定する。
     * {@code pc_assets.agent_number} で照合する。
     * 2回目以降のレポートはこのメソッドで検索する。</p>
     *
     * @param agentNumber エージェント番号（例: "AGT-A1B2C3D4"）
     * @return 対応する PcAsset エンティティ（存在しない場合は null）
     */
    PcAsset findAssetByAgentNumber(@Param("agentNumber") String agentNumber);

    /**
     * ホスト名でPC資産を検索する（初回紐付け用フォールバック）
     *
     * <p>エージェントが初めてレポートを送信する際（{@code pc_assets.agent_number} が未設定）に
     * ホスト名で PC 資産を特定する。
     * 紐付け後は {@link #findAssetByAgentNumber} を使用する。</p>
     *
     * @param hostname エージェントが報告したホスト名
     * @return 対応する PcAsset エンティティ（存在しない場合は null）
     */
    PcAsset findAssetByHostname(@Param("hostname") String hostname);

    /**
     * PC資産にエージェント番号を紐付ける（初回レポート時の一回限り操作）
     *
     * <p>ホスト名で資産を特定した後、{@code pc_assets.agent_number} を設定して
     * 以後エージェント番号による検索を可能にする。</p>
     *
     * @param agentNumber 紐付けるエージェント番号
     * @param hostname    対象PC資産のホスト名
     * @return 更新件数（通常 1）
     */
    int linkAgentNumberToAsset(@Param("agentNumber") String agentNumber,
                               @Param("hostname") String hostname);

    // ---- ハードウェア情報 (pc_hardware_info テーブル) ----

    /**
     * 指定エージェントのハードウェア情報を取得する（upsert の存在確認用）
     *
     * <p>V4 変更: {@code pc_asset_id} から {@code agent_number} によるキー検索に変更。</p>
     *
     * @param agentNumber 対象エージェント番号
     * @return PcHardwareInfo エンティティ（未登録の場合は null）
     */
    PcHardwareInfo findHardwareByAgentNumber(@Param("agentNumber") String agentNumber);

    /**
     * ハードウェア情報を新規登録する（初回報告時）
     *
     * @param info 登録するハードウェア情報エンティティ（agentNumber フィールドが必須）
     * @return 挿入件数（通常 1）
     */
    int insertHardware(PcHardwareInfo info);

    /**
     * ハードウェア情報を更新する（2回目以降の報告時）
     *
     * @param info 更新するハードウェア情報エンティティ（agentNumber フィールドが必須）
     * @return 更新件数（通常 1）
     */
    int updateHardware(PcHardwareInfo info);

    // ---- ソフトウェア情報 (pc_software_info テーブル) ----

    /**
     * 指定エージェントのソフトウェア情報を全件削除する（再登録前の全削除）
     *
     * <p>V4 変更: {@code pc_asset_id} から {@code agent_number} によるキー削除に変更。</p>
     *
     * @param agentNumber 対象エージェント番号
     * @return 削除件数
     */
    int deleteSoftwareByAgentNumber(@Param("agentNumber") String agentNumber);

    /**
     * ソフトウェア情報を1件登録する
     *
     * <p>V4 変更: {@code pcAssetId} から {@code agentNumber} に変更。</p>
     *
     * @param agentNumber  対象エージェント番号
     * @param softwareName ソフトウェア名
     * @param version      バージョン
     * @param publisher    発行元
     * @param installDate  インストール日
     * @return 挿入件数（通常 1）
     */
    int insertSoftware(@Param("agentNumber") String agentNumber,
                       @Param("softwareName") String softwareName,
                       @Param("version") String version,
                       @Param("publisher") String publisher,
                       @Param("installDate") String installDate);

    // ---- ネットワーク情報 (pc_network_info テーブル) ----

    /**
     * 指定エージェントのネットワーク情報を全件削除する（再登録前の全削除）
     *
     * <p>V4 変更: {@code pc_asset_id} から {@code agent_number} によるキー削除に変更。</p>
     *
     * @param agentNumber 対象エージェント番号
     * @return 削除件数
     */
    int deleteNetworkByAgentNumber(@Param("agentNumber") String agentNumber);

    /**
     * ネットワーク情報（NIC 1枚分）を登録する
     *
     * <p>V4 変更: {@code pcAssetId} から {@code agentNumber} に変更。</p>
     *
     * @param agentNumber 対象エージェント番号
     * @param nicName     NIC 名称（例: "Wi-Fi"）
     * @param ipAddress   IP アドレス（例: "192.168.1.10"）
     * @param macAddress  MAC アドレス（例: "AA:BB:CC:DD:EE:FF"）
     * @return 挿入件数（通常 1）
     */
    int insertNetwork(@Param("agentNumber") String agentNumber,
                      @Param("nicName") String nicName,
                      @Param("ipAddress") String ipAddress,
                      @Param("macAddress") String macAddress);

    // ---- 使用者名 → 社員ID 変換 ----

    /**
     * 社員のフルネームで社員IDを検索する（エージェント報告の使用者名照合用）
     *
     * <p>エージェントが報告した使用者名と {@code employees.full_name} を照合し、
     * 一致した社員の ID を返す。同姓同名が存在する場合は最初の1件の ID を返す。
     * 一致する社員が存在しない場合は null を返す。</p>
     *
     * @param fullName 使用者名（エージェント設定画面で入力した値）
     * @return 社員ID（一致しない場合は null）
     */
    Long findEmployeeIdByName(@Param("fullName") String fullName);

    // ---- PC資産更新 (pc_assets テーブル) ----

    /**
     * エージェントの最終接続日時・設置場所・使用者を更新する
     *
     * <p>V4 変更: WHERE 条件を {@code id} から {@code agent_number} に変更。<br>
     * エージェントからの報告を受信するたびに呼び出され、以下を更新する:
     * <ul>
     *   <li>{@code agent_last_seen}: 現在日時（常に更新）</li>
     *   <li>{@code location}: エージェントが報告した設置場所（null / 空文字の場合は更新しない）</li>
     *   <li>{@code user_name}: エージェントが報告した使用者名テキスト（null / 空文字の場合は更新しない）</li>
     *   <li>{@code assigned_employee_id}: 使用者として照合された社員ID（null の場合は更新しない）</li>
     * </ul>
     * </p>
     *
     * @param agentNumber        対象エージェント番号（WHERE 条件として使用）
     * @param location           エージェントが報告した設置場所（null の場合は更新しない）
     * @param userName           エージェントが報告した使用者名（null の場合は更新しない）
     * @param assignedEmployeeId 使用者として照合された社員ID（null の場合は更新しない）
     * @return 更新件数（通常 1）
     */
    int updateAgentLastSeen(@Param("agentNumber") String agentNumber,
                            @Param("location") String location,
                            @Param("userName") String userName,
                            @Param("assignedEmployeeId") Long assignedEmployeeId);

    /**
     * PC資産の取得区分を更新する
     *
     * <p>V4 変更: WHERE 条件を {@code id} から {@code agent_number} に変更。<br>
     * エージェントが設定画面で「購入」または「レンタル」を選択して保存した場合に呼び出す。
     * バックエンドに既に値が設定済みの場合はエージェント側がdisabledになるため、このメソッドは呼ばれない。
     * 更新対象カラム: {@code pc_assets.acquisition_type}</p>
     *
     * @param agentNumber     対象エージェント番号（WHERE 条件として使用）
     * @param acquisitionType 設定する取得区分（"PURCHASE" または "RENTAL"）
     * @return 更新件数（通常 1）
     */
    int updateAcquisitionType(@Param("agentNumber") String agentNumber,
                              @Param("acquisitionType") String acquisitionType);
}
