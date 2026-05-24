package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.api.dto.request.EmployeeSearchRequest;
import com.company.pcmgmt.api.dto.response.EmployeeResponse;
import com.company.pcmgmt.domain.entity.Employee;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EmployeeMapper {

    List<EmployeeResponse> findAll(@Param("req") EmployeeSearchRequest req);

    long countAll(@Param("req") EmployeeSearchRequest req);

    EmployeeResponse findById(@Param("id") Long id);

    Employee findRawById(@Param("id") Long id);

    int insert(Employee employee);

    int update(Employee employee);

    boolean existsByEmployeeCode(@Param("employeeCode") String employeeCode,
                                 @Param("excludeId") Long excludeId);

    /** 社員選択プルダウン用（在籍者のみ） */
    List<EmployeeResponse> findActiveList();
}
