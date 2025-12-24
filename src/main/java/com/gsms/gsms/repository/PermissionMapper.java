package com.gsms.gsms.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限 Mapper
 */
@Mapper
public interface PermissionMapper {

    /**
     * 根据用户ID查询其拥有的权限编码列表
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);
}
