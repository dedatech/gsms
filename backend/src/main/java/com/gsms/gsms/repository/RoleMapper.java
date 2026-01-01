package com.gsms.gsms.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper {

    /**
     * 根据用户ID查询其拥有的角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
