package com.company.pcmgmt.api.dto.request;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class AgentReportRequest {

    /** エージェントが動作しているPCのホスト名 */
    private String hostname;

    /** ハードウェア情報 */
    private String cpuModel;
    private Integer cpuCores;
    private BigDecimal memoryGb;
    private BigDecimal diskGb;
    private BigDecimal diskFreeGb;

    /** OS情報 */
    private String osName;
    private String osVersion;

    /** ネットワーク情報 */
    private List<NetworkInterface> networkInterfaces;

    /** インストール済みソフトウェア */
    private List<InstalledSoftware> installedSoftware;

    @Data
    public static class NetworkInterface {
        private String nicName;
        private String ipAddress;
        private String macAddress;
    }

    @Data
    public static class InstalledSoftware {
        private String softwareName;
        private String version;
        private String publisher;
        private String installDate;
    }
}
