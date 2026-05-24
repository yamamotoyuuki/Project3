package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.api.dto.request.RentalSearchRequest;
import com.company.pcmgmt.api.dto.response.RentalResponse;
import com.company.pcmgmt.api.dto.response.RentalVendorResponse;
import com.company.pcmgmt.domain.entity.PcAcquisitionRental;
import com.company.pcmgmt.domain.entity.RentalVendor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RentalMapper {

    // ---- レンタル契約 ----
    List<RentalResponse> findAll(@Param("req") RentalSearchRequest req);

    long countAll(@Param("req") RentalSearchRequest req);

    RentalResponse findById(@Param("id") Long id);

    PcAcquisitionRental findRawById(@Param("id") Long id);

    int insert(PcAcquisitionRental rental);

    int update(PcAcquisitionRental rental);

    /** ダッシュボード: 90日以内に期限切れ（未返却） */
    long countNearExpiry();

    /** ダッシュボード: 期限切れ（未返却） */
    long countExpired();

    // ---- ベンダー ----
    List<RentalVendorResponse> findAllVendors();

    RentalVendorResponse findVendorById(@Param("id") Long id);

    RentalVendor findRawVendorById(@Param("id") Long id);

    int insertVendor(RentalVendor vendor);

    int updateVendor(RentalVendor vendor);
}
