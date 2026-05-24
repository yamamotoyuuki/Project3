package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.api.dto.request.SoftwareSearchRequest;
import com.company.pcmgmt.api.dto.response.SoftwareResponse;
import com.company.pcmgmt.domain.entity.SoftwareMaster;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SoftwareMapper {

    List<SoftwareResponse> findAll(@Param("req") SoftwareSearchRequest req);

    long countAll(@Param("req") SoftwareSearchRequest req);

    SoftwareResponse findById(@Param("id") Long id);

    SoftwareMaster findRawById(@Param("id") Long id);

    int insert(SoftwareMaster software);

    int update(SoftwareMaster software);

    /** ダッシュボード: ライセンス超過件数 */
    long countOverLimit();
}
