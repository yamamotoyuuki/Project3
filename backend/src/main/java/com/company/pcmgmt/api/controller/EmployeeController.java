package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.request.EmployeeCreateRequest;
import com.company.pcmgmt.api.dto.request.EmployeeSearchRequest;
import com.company.pcmgmt.api.dto.request.EmployeeUpdateRequest;
import com.company.pcmgmt.api.dto.response.ApiResponse;
import com.company.pcmgmt.api.dto.response.EmployeeResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 社員一覧取得
     * GET /api/v1/employees?page=0&size=20&keyword=xxx&isActive=true
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<EmployeeResponse>>> findAll(
            EmployeeSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.findAll(req)));
    }

    /**
     * 在籍社員リスト（プルダウン用）
     * GET /api/v1/employees/active
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> findActiveList() {
        return ResponseEntity.ok(ApiResponse.success(employeeService.findActiveList()));
    }

    /**
     * 社員詳細取得
     * GET /api/v1/employees/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(employeeService.findById(id)));
    }

    /**
     * 社員登録
     * POST /api/v1/employees
     */
    @PostMapping
    public ResponseEntity<ApiResponse<EmployeeResponse>> create(
            @Valid @RequestBody EmployeeCreateRequest req) {
        EmployeeResponse created = employeeService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("社員を登録しました", created));
    }

    /**
     * 社員更新
     * PUT /api/v1/employees/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EmployeeResponse>> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("社員情報を更新しました", employeeService.update(id, req)));
    }
}
