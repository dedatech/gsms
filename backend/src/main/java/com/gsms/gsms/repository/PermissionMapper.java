package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.Permission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 权限 Mapper
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据用户ID查询其拥有的权限编码列表
     */
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据ID查询权限
     */
    Permission selectById(@Param("id") Long id);

    /**
     * 根据编码查询权限
     */
    Permission selectByCode(@Param("code") String code);

    /**
     * 根据条件查询权限列表
     */
    List<Permission> selectByCondition(@Param("name") String name, @Param("code") String code);

    /**
     * 查询所有权限
     */
    List<Permission> selectAll();

    /**
     * 插入权限
     */
    int insert(Permission permission);

    /**
     * 更新权限
     */
    int update(Permission permission);

    /**
     * 根据ID删除权限（软删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询拥有该权限的角色ID列表
     */
    List<Long> selectRoleIdsByPermissionId(@Param("permissionId") Long permissionId);
}
