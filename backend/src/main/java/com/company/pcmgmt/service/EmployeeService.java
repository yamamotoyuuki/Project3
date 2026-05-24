package com.company.pcmgmt.service;

import com.company.pcmgmt.api.dto.request.EmployeeCreateRequest;
import com.company.pcmgmt.api.dto.request.EmployeeSearchRequest;
import com.company.pcmgmt.api.dto.request.EmployeeUpdateRequest;
import com.company.pcmgmt.api.dto.response.EmployeeResponse;
import com.company.pcmgmt.api.dto.response.PageResponse;
import com.company.pcmgmt.domain.entity.Employee;
import com.company.pcmgmt.domain.mapper.EmployeeMapper;
import com.company.pcmgmt.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeMapper employeeMapper;

    /**
     * 社員一覧取得
     */
    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> findAll(EmployeeSearchRequest req) {
        List<EmployeeResponse> content = employeeMapper.findAll(req);
        long total = employeeMapper.countAll(req);
        return PageResponse.of(content, total, req.getPage(), req.getSize());
    }

    /**
     * 在籍社員リスト（プルダウン用）
     */
    @Transactional(readOnly = true)
    public List<EmployeeResponse> findActiveList() {
        return employeeMapper.findActiveList();
    }

    /**
     * 社員詳細取得
     */
    @Transactional(readOnly = true)
    public EmployeeResponse findById(Long id) {
        EmployeeResponse emp = employeeMapper.findById(id);
        if (emp == null) {
            throw new ResourceNotFoundException("社員が見つかりません: id=" + id);
        }
        return emp;
    }

    /**
     * 社員登録
     */
    @Transactional
    public EmployeeResponse create(EmployeeCreateRequest req) {
        if (employeeMapper.existsByEmployeeCode(req.getEmployeeCode(), null)) {
            throw new IllegalArgumentException("社員コード [" + req.getEmployeeCode() + "] は既に使用されています");
        }

        Employee emp = new Employee();
        emp.setEmployeeCode(req.getEmployeeCode());
        emp.setFullName(req.getFullName());
        emp.setDepartment(req.getDepartment());
        emp.setPosition(req.getPosition());
        emp.setEmail(req.getEmail());
        emp.setPhone(req.getPhone());
        emp.setLocation(req.getLocation());
        emp.setIsActive(true);

        employeeMapper.insert(emp);
        log.info("社員登録: employeeCode={}", emp.getEmployeeCode());
        return employeeMapper.findById(emp.getId());
    }

    /**
     * 社員更新
     */
    @Transactional
    public EmployeeResponse update(Long id, EmployeeUpdateRequest req) {
        Employee existing = employeeMapper.findRawById(id);
        if (existing == null) {
            throw new ResourceNotFoundException("社員が見つかりません: id=" + id);
        }

        existing.setFullName(req.getFullName());
        existing.setDepartment(req.getDepartment());
        existing.setPosition(req.getPosition());
        existing.setEmail(req.getEmail());
        existing.setPhone(req.getPhone());
        existing.setLocation(req.getLocation());
        if (req.getIsActive() != null) {
            existing.setIsActive(req.getIsActive());
        }

        employeeMapper.update(existing);
        log.info("社員更新: id={}", id);
        return employeeMapper.findById(id);
    }
}
