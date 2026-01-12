package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.Role;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色 Mapper
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据用户ID查询其拥有的角色编码列表
     */
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据ID查询角色
     */
    Role selectById(@Param("id") Long id);

    /**
     * 根据编码查询角色
     */
    Role selectByCode(@Param("code") String code);

    /**
     * 根据条件查询角色列表
     */
    List<Role> selectByCondition(@Param("name") String name, @Param("code") String code, @Param("roleLevel") Integer roleLevel);

    /**
     * 查询所有角色
     */
    List<Role> selectAll();

    /**
     * 插入角色
     */
    int insert(Role role);

    /**
     * 更新角色
     */
    int update(Role role);

    /**
     * 根据ID删除角色（软删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询角色的权限ID列表
     */
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除角色的所有权限
     */
    int deletePermissionsByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色权限关联
     */
    int insertRolePermissions(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);

    /**
     * 删除角色权限关联
     */
    int deleteRolePermission(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 查询拥有该角色的用户ID列表
     */
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 查询用户的角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 删除用户的所有角色
     */
    int deleteUserRoles(@Param("userId") Long userId);

    /**
     * 批量插入用户角色关联
     */
    int insertUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    /**
     * 删除用户角色关联
     */
    int deleteUserRole(@Param("userId") Long userId, @Param("roleId") Long roleId);
}
