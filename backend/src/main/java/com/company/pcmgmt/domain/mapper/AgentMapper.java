package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.domain.entity.PcAsset;
import com.company.pcmgmt.domain.entity.PcHardwareInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AgentMapper {

    PcAsset findAssetByHostname(@Param("hostname") String hostname);

    /** pc_hardware_info: upsert */
    PcHardwareInfo findHardwareByAssetId(@Param("pcAssetId") Long pcAssetId);
    int insertHardware(PcHardwareInfo info);
    int updateHardware(PcHardwareInfo info);

    /** pc_software_info: 既存削除 + 再INSERT */
    int deleteSoftwareByAssetId(@Param("pcAssetId") Long pcAssetId);
    int insertSoftware(@Param("pcAssetId") Long pcAssetId,
                       @Param("softwareName") String softwareName,
                       @Param("version") String version,
                       @Param("publisher") String publisher,
                       @Param("installDate") String installDate);

    /** pc_network_info: 既存削除 + 再INSERT */
    int deleteNetworkByAssetId(@Param("pcAssetId") Long pcAssetId);
    int insertNetwork(@Param("pcAssetId") Long pcAssetId,
                      @Param("nicName") String nicName,
                      @Param("ipAddress") String ipAddress,
                      @Param("macAddress") String macAddress);

    /** agent_last_seen 更新 */
    int updateAgentLastSeen(@Param("pcAssetId") Long pcAssetId);
}
