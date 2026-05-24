package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.RentalCreateRequest;
import com.company.pcmgmt.api.dto.request.RentalSearchRequest;
import com.company.pcmgmt.api.dto.request.RentalVendorCreateRequest;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.api.dto.response.RentalResponse;
import com.company.pcmgmt.api.dto.response.RentalVendorResponse;
import com.company.pcmgmt.domain.entity.PcAcquisitionRental;
import com.company.pcmgmt.domain.entity.RentalVendor;
import com.company.pcmgmt.domain.mapper.PcAssetMapper;
import com.company.pcmgmt.domain.mapper.RentalMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    private final RentalMapper rentalMapper;
    private final PcAssetMapper pcAssetMapper;

    // ======= レンタル契約 =======

    @Transactional(readOnly = true)
    public PageResponse<RentalResponse> findAll(RentalSearchRequest req) {
        List<RentalResponse> content = rentalMapper.findAll(req);
        long total = rentalMapper.countAll(req);
        return PageResponse.of(content, total, req.getPage(), req.getSize());
    }

    @Transactional(readOnly = true)
    public RentalResponse findById(Long id) {
        RentalResponse r = rentalMapper.findById(id);
        if (r == null) throw new ResourceNotFoundException("レンタル契約が見つかりません: id=" + id);
        return r;
    }

    @Transactional
    public RentalResponse create(RentalCreateRequest req) {
        if (pcAssetMapper.findRawById(req.getPcAssetId()) == null) {
            throw new ResourceNotFoundException("PC資産が見つかりません: id=" + req.getPcAssetId());
        }
        if (rentalMapper.findRawVendorById(req.getRentalVendorId()) == null) {
            throw new ResourceNotFoundException("ベンダーが見つかりません: id=" + req.getRentalVendorId());
        }

        PcAcquisitionRental rental = new PcAcquisitionRental();
        rental.setPcAssetId(req.getPcAssetId());
        rental.setRentalVendorId(req.getRentalVendorId());
        rental.setContractNumber(req.getContractNumber());
        rental.setRentalStartDate(req.getRentalStartDate());
        rental.setRentalEndDate(req.getRentalEndDate());
        rental.setMonthlyFee(req.getMonthlyFee());
        rental.setContractFilePath(req.getContractFilePath());

        rentalMapper.insert(rental);
        log.info("レンタル契約登録: id={}", rental.getId());
        return rentalMapper.findById(rental.getId());
    }

    @Transactional
    public RentalResponse returnRental(Long id) {
        PcAcquisitionRental rental = rentalMapper.findRawById(id);
        if (rental == null) throw new ResourceNotFoundException("レンタル契約が見つかりません: id=" + id);
        if (rental.getReturnDate() != null) throw new IllegalStateException("この契約はすでに返却済みです");

        rental.setReturnDate(LocalDate.now());
        rentalMapper.update(rental);
        log.info("レンタル返却登録: id={}", id);
        return rentalMapper.findById(id);
    }

    // ======= ベンダー =======

    @Transactional(readOnly = true)
    public List<RentalVendorResponse> findAllVendors() {
        return rentalMapper.findAllVendors();
    }

    @Transactional(readOnly = true)
    public RentalVendorResponse findVendorById(Long id) {
        RentalVendorResponse v = rentalMapper.findVendorById(id);
        if (v == null) throw new ResourceNotFoundException("ベンダーが見つかりません: id=" + id);
        return v;
    }

    @Transactional
    public RentalVendorResponse createVendor(RentalVendorCreateRequest req) {
        RentalVendor vendor = new RentalVendor();
        vendor.setCompanyName(req.getCompanyName());
        vendor.setContactName(req.getContactName());
        vendor.setPhone(req.getPhone());
        vendor.setEmail(req.getEmail());
        vendor.setAddress(req.getAddress());
        vendor.setNote(req.getNote());
        rentalMapper.insertVendor(vendor);
        log.info("ベンダー登録: id={}", vendor.getId());
        return rentalMapper.findVendorById(vendor.getId());
    }

    @Transactional
    public RentalVendorResponse updateVendor(Long id, RentalVendorCreateRequest req) {
        RentalVendor vendor = rentalMapper.findRawVendorById(id);
        if (vendor == null) throw new ResourceNotFoundException("ベンダーが見つかりません: id=" + id);
        vendor.setCompanyName(req.getCompanyName());
        vendor.setContactName(req.getContactName());
        vendor.setPhone(req.getPhone());
        vendor.setEmail(req.getEmail());
        vendor.setAddress(req.getAddress());
        vendor.setNote(req.getNote());
        rentalMapper.updateVendor(vendor);
        return rentalMapper.findVendorById(id);
    }
}
