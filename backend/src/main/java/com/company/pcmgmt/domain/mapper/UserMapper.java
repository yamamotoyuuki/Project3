package com.company.pcmgmt.domain.mapper;

import com.company.pcmgmt.domain.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {
    User findByUsername(@Param("username") String username);
    User findById(@Param("id") Long id);
    void updateLastLoginAt(@Param("id") Long id);
    void insert(User user);
}
