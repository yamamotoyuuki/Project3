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

@Slf4j
@Service
@RequiredArgsConstructor
public class AgentService {

    private final AgentMapper agentMapper;

    @Transactional
    public String processReport(AgentReportRequest req) {
        if (req.getHostname() == null || req.getHostname().isBlank()) {
            return "HOSTNAME_MISSING";
        }

        PcAsset asset = agentMapper.findAssetByHostname(req.getHostname());
        if (asset == null) {
            log.warn("エージェント報告: ホスト名 '{}' に対応するPC資産が見つかりません", req.getHostname());
            return "ASSET_NOT_FOUND";
        }

        Long assetId = asset.getId();

        // ハードウェア情報 upsert
        PcHardwareInfo existing = agentMapper.findHardwareByAssetId(assetId);
        PcHardwareInfo hw = new PcHardwareInfo();
        hw.setPcAssetId(assetId);
        hw.setCpuModel(req.getCpuModel());
        hw.setCpuCores(req.getCpuCores());
        hw.setMemoryGb(req.getMemoryGb());
        hw.setDiskGb(req.getDiskGb());
        hw.setDiskFreeGb(req.getDiskFreeGb());

        if (existing == null) {
            agentMapper.insertHardware(hw);
        } else {
            agentMapper.updateHardware(hw);
        }

        // ソフトウェア情報 全削除→再登録
        agentMapper.deleteSoftwareByAssetId(assetId);
        if (!CollectionUtils.isEmpty(req.getInstalledSoftware())) {
            for (AgentReportRequest.InstalledSoftware sw : req.getInstalledSoftware()) {
                agentMapper.insertSoftware(
                    assetId, sw.getSoftwareName(), sw.getVersion(),
                    sw.getPublisher(), sw.getInstallDate()
                );
            }
        }

        // ネットワーク情報 全削除→再登録
        agentMapper.deleteNetworkByAssetId(assetId);
        if (!CollectionUtils.isEmpty(req.getNetworkInterfaces())) {
            for (AgentReportRequest.NetworkInterface nic : req.getNetworkInterfaces()) {
                agentMapper.insertNetwork(
                    assetId, nic.getNicName(), nic.getIpAddress(), nic.getMacAddress()
                );
            }
        }

        // agent_last_seen 更新
        agentMapper.updateAgentLastSeen(assetId);

        log.info("エージェント報告受信: hostname={}, assetId={}", req.getHostname(), assetId);
        return "OK";
    }
}
