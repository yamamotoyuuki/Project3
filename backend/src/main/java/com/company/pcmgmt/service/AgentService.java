package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.AgentReportRequest;
import com.company.pcmgmt.domain.entity.PcAsset;
import com.company.pcmgmt.domain.entity.PcHardwareInfo;
import com.company.pcmgmt.domain.mapper.AgentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

/**
 * エージェント情報収集サービス
 *
 * <p>Tauri エージェントから受信したハードウェア・ソフトウェア・ネットワーク情報を
 * データベースに保存する処理を担当する。</p>
 *
 * <p>処理フロー:
 * <ol>
 *   <li>ホスト名でPC資産を検索して紐付け</li>
 *   <li>ハードウェア情報を upsert（初回: INSERT、2回目以降: UPDATE）</li>
 *   <li>ソフトウェア情報を全削除→再登録（DELETE + INSERT）</li>
 *   <li>ネットワーク情報を全削除→再登録（DELETE + INSERT）</li>
 *   <li>エージェント最終接続日時を更新</li>
 * </ol>
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    /** エージェント関連の DB アクセスを担うマッパー */
    private final AgentMapper agentMapper;

    /**
     * エージェントからの報告を処理してDBに保存する
     *
     * @param req エージェント報告リクエスト（ハードウェア・ソフトウェア・ネットワーク情報）
     * @return 処理結果コード
     *         <ul>
     *           <li>"HOSTNAME_MISSING": ホスト名が未指定</li>
     *           <li>"ASSET_NOT_FOUND": 対応するPC資産が見つからない</li>
     *           <li>"OK": 正常処理完了</li>
     *         </ul>
     */
    @Transactional
    public String processReport(AgentReportRequest req) {
        // ホスト名が未指定の場合はエラーコードを返す
        if (req.getHostname() == null || req.getHostname().isBlank()) {
            return "HOSTNAME_MISSING";
        }

        // ホスト名でPC資産を検索（エージェントとPC資産の紐付け）
        PcAsset asset = agentMapper.findAssetByHostname(req.getHostname());
        if (asset == null) {
            // 対応するPC資産が見つからない場合は警告ログを出力して終了
            log.warn("エージェント報告: ホスト名 '{}' に対応するPC資産が見つかりません", req.getHostname());
            return "ASSET_NOT_FOUND";
        }

        // 対象PC資産のID
        Long assetId = asset.getId();

        // ---- ハードウェア情報を upsert（存在すれば UPDATE、なければ INSERT）----
        PcHardwareInfo existing = agentMapper.findHardwareByAssetId(assetId);
        // ハードウェア情報エンティティを組み立て
        PcHardwareInfo hw = new PcHardwareInfo();
        hw.setPcAssetId(assetId);              // 対象PC資産ID
        hw.setCpuModel(req.getCpuModel());     // CPU モデル名
        hw.setCpuCores(req.getCpuCores());     // CPU コア数
        hw.setMemoryGb(req.getMemoryGb());     // メモリ容量(GB)
        hw.setDiskGb(req.getDiskGb());         // ディスク容量(GB)
        hw.setDiskFreeGb(req.getDiskFreeGb()); // ディスク空き容量(GB)

        if (existing == null) {
            // 初回報告: 新規INSERT
            agentMapper.insertHardware(hw);
        } else {
            // 2回目以降: 更新
            agentMapper.updateHardware(hw);
        }

        // ---- ソフトウェア情報を全削除→再登録（最新状態に置き換え）----
        agentMapper.deleteSoftwareByAssetId(assetId);
        if (!CollectionUtils.isEmpty(req.getInstalledSoftware())) {
            // インストール済みソフトウェアを1件ずつ登録
            for (AgentReportRequest.InstalledSoftware sw : req.getInstalledSoftware()) {
                agentMapper.insertSoftware(
                    assetId,               // 対象PC資産ID
                    sw.getSoftwareName(),  // ソフトウェア名
                    sw.getVersion(),       // バージョン
                    sw.getPublisher(),     // 発行元
                    sw.getInstallDate()    // インストール日
                );
            }
        }

        // ---- ネットワーク情報を全削除→再登録（最新状態に置き換え）----
        agentMapper.deleteNetworkByAssetId(assetId);
        if (!CollectionUtils.isEmpty(req.getNetworkInterfaces())) {
            // NIC 情報を1件ずつ登録
            for (AgentReportRequest.NetworkInterface nic : req.getNetworkInterfaces()) {
                agentMapper.insertNetwork(
                    assetId,              // 対象PC資産ID
                    nic.getNicName(),     // NIC 名称
                    nic.getIpAddress(),   // IP アドレス
                    nic.getMacAddress()   // MAC アドレス
                );
            }
        }

        // ---- エージェント最終接続日時を現在時刻に更新 ----
        agentMapper.updateAgentLastSeen(assetId);

        log.info("エージェント報告受信: hostname={}, assetId={}", req.getHostname(), assetId);
        return "OK";
    }
}
