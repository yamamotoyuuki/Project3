package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.SoftwareCreateRequest;
import com.company.pcmgmt.api.dto.request.SoftwareSearchRequest;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.api.dto.response.SoftwareResponse;
import com.company.pcmgmt.domain.entity.SoftwareMaster;
import com.company.pcmgmt.domain.mapper.SoftwareMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SoftwareService {

    private final SoftwareMapper softwareMapper;

    @Transactional(readOnly = true)
    public PageResponse<SoftwareResponse> findAll(SoftwareSearchRequest req) {
        List<SoftwareResponse> content = softwareMapper.findAll(req);
        long total = softwareMapper.countAll(req);
        return PageResponse.of(content, total, req.getPage(), req.getSize());
    }

    @Transactional(readOnly = true)
    public SoftwareResponse findById(Long id) {
        SoftwareResponse sw = softwareMapper.findById(id);
        if (sw == null) throw new ResourceNotFoundException("ソフトウェアが見つかりません: id=" + id);
        return sw;
    }

    @Transactional
    public SoftwareResponse create(SoftwareCreateRequest req) {
        SoftwareMaster sw = new SoftwareMaster();
        sw.setSoftwareName(req.getSoftwareName());
        sw.setPublisher(req.getPublisher());
        sw.setLicenseType(req.getLicenseType());
        sw.setPurchasedCount(req.getPurchasedCount() != null ? req.getPurchasedCount() : 0);
        sw.setNote(req.getNote());
        softwareMapper.insert(sw);
        log.info("ソフトウェア登録: id={}", sw.getId());
        return softwareMapper.findById(sw.getId());
    }

    @Transactional
    public SoftwareResponse update(Long id, SoftwareCreateRequest req) {
        SoftwareMaster sw = softwareMapper.findRawById(id);
        if (sw == null) throw new ResourceNotFoundException("ソフトウェアが見つかりません: id=" + id);
        sw.setSoftwareName(req.getSoftwareName());
        sw.setPublisher(req.getPublisher());
        sw.setLicenseType(req.getLicenseType());
        sw.setPurchasedCount(req.getPurchasedCount() != null ? req.getPurchasedCount() : 0);
        sw.setNote(req.getNote());
        softwareMapper.update(sw);
        return softwareMapper.findById(id);
    }
}
