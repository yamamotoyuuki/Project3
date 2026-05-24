package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.request.LoanCreateRequest;
import com.company.pcmgmt.api.dto.request.LoanReturnRequest;
import com.company.pcmgmt.api.dto.request.LoanSearchRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.LoanResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.service.LoanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/loans")
@RequiredArgsConstructor
public class LoanController {

    private final LoanService loanService;

    /** GET /api/v1/loans */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<LoanResponse>>> findAll(LoanSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success(loanService.findAll(req)));
    }

    /** GET /api/v1/loans/{id} */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LoanResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(loanService.findById(id)));
    }

    /** POST /api/v1/loans */
    @PostMapping
    public ResponseEntity<ApiResponse<LoanResponse>> create(
            @Valid @RequestBody LoanCreateRequest req) {
        LoanResponse created = loanService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("貸出を登録しました", created));
    }

    /** PUT /api/v1/loans/{id}/return */
    @PutMapping("/{id}/return")
    public ResponseEntity<ApiResponse<LoanResponse>> returnLoan(
            @PathVariable Long id,
            @Valid @RequestBody LoanReturnRequest req) {
        return ResponseEntity.ok(ApiResponse.success("返却を登録しました", loanService.returnLoan(id, req)));
    }
}
