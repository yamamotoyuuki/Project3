package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.request.AgentReportRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.service.AgentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agent")
@RequiredArgsConstructor
public class AgentController {

    private final AgentService agentService;

    /**
     * エージェントからのハードウェア・ソフトウェア・ネットワーク情報受信
     * POST /api/v1/agent/report
     * ※ SecurityConfig で認証不要に設定済み
     */
    @PostMapping("/report")
    public ResponseEntity<ApiResponse<String>> report(
            @RequestBody AgentReportRequest req) {
        String result = agentService.processReport(req);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
}
