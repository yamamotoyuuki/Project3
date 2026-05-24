package com.company.pcmgmt.api.controller;

import com.company.pcmgmt.api.dto.request.RentalCreateRequest;
import com.company.pcmgmt.api.dto.request.RentalSearchRequest;
import com.company.pcmgmt.api.dto.request.RentalVendorCreateRequest;
import com.company.pcmgmt.api.dto.response.*;
import com.company.pcmgmt.service.RentalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class RentalController {

    private final RentalService rentalService;

    // ======= レンタル契約 /api/v1/rentals =======

    @GetMapping("/api/v1/rentals")
    public ResponseEntity<ApiResponse<PageResponse<RentalResponse>>> findAll(RentalSearchRequest req) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.findAll(req)));
    }

    @GetMapping("/api/v1/rentals/{id}")
    public ResponseEntity<ApiResponse<RentalResponse>> findById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.findById(id)));
    }

    @PostMapping("/api/v1/rentals")
    public ResponseEntity<ApiResponse<RentalResponse>> create(@Valid @RequestBody RentalCreateRequest req) {
        RentalResponse created = rentalService.create(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("レンタル契約を登録しました", created));
    }

    @PutMapping("/api/v1/rentals/{id}/return")
    public ResponseEntity<ApiResponse<RentalResponse>> returnRental(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("返却を登録しました", rentalService.returnRental(id)));
    }

    // ======= ベンダー /api/v1/rental-vendors =======

    @GetMapping("/api/v1/rental-vendors")
    public ResponseEntity<ApiResponse<List<RentalVendorResponse>>> findAllVendors() {
        return ResponseEntity.ok(ApiResponse.success(rentalService.findAllVendors()));
    }

    @GetMapping("/api/v1/rental-vendors/{id}")
    public ResponseEntity<ApiResponse<RentalVendorResponse>> findVendorById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(rentalService.findVendorById(id)));
    }

    @PostMapping("/api/v1/rental-vendors")
    public ResponseEntity<ApiResponse<RentalVendorResponse>> createVendor(
            @Valid @RequestBody RentalVendorCreateRequest req) {
        RentalVendorResponse created = rentalService.createVendor(req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("ベンダーを登録しました", created));
    }

    @PutMapping("/api/v1/rental-vendors/{id}")
    public ResponseEntity<ApiResponse<RentalVendorResponse>> updateVendor(
            @PathVariable Long id,
            @Valid @RequestBody RentalVendorCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.success("ベンダーを更新しました", rentalService.updateVendor(id, req)));
    }
}
