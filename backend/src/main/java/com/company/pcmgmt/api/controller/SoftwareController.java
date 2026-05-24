package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.request.SoftwareCreateRequest;
import com.company.pcmgmt.api.dto.request.SoftwareSearchRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.api.dto.response.SoftwareResponse;
import com.company.pcmgmt.service.SoftwareService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/software")
@RequiredArgsConstructor
public class SoftwareController {

    private final SoftwareService softwareService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SoftwareResponse>>> findAll(SoftwareSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success(softwareService.findAll(req)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SoftwareResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(softwareService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SoftwareResponse>> create(@Valid @RequestBody SoftwareCreateRequest req) {
        SoftwareResponse created = softwareService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("ソフトウェアを登録しました", created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SoftwareResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody SoftwareCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("ソフトウェアを更新しました", softwareService.update(id, req)));
    }
}
