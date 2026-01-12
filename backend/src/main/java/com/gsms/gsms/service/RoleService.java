package com.gsms.gsms.service;

import com.gsms.gsms.dto.role.RoleCreateReq;
import com.gsms.gsms.dto.role.RoleInfoResp;
import com.gsms.gsms.dto.role.RolePermissionAssignReq;
import com.gsms.gsms.dto.role.RoleQueryReq;
import com.gsms.gsms.dto.role.RoleUpdateReq;
import com.gsms.gsms.infra.common.PageResult;

import java.util.List;

/**
 * 角色服务接口
 */
public interface RoleService {

    /**
     * 根据ID查询角色
     * @param id 角色ID
     * @return 角色响应DTO
     */
    RoleInfoResp getById(Long id);

    /**
     * 根据条件分页查询角色
     * @param req 查询条件
     * @return 分页结果
     */
    PageResult<RoleInfoResp> findAll(RoleQueryReq req);

    /**
     * 创建角色
     * @param createReq 创建请求DTO
     * @return 创建成功的角色响应DTO
     */
    RoleInfoResp create(RoleCreateReq createReq);

    /**
     * 更新角色
     * @param updateReq 更新请求DTO
     * @return 更新后的角色响应DTO
     */
    RoleInfoResp update(RoleUpdateReq updateReq);

    /**
     * 删除角色
     * @param id 角色ID
     */
    void delete(Long id);

    /**
     * 为角色分配权限
     * @param req 角色权限分配请求
     */
    void assignPermissions(RolePermissionAssignReq req);

    /**
     * 查询角色的权限ID列表
     * @param roleId 角色ID
     * @return 权限ID列表
     */
    List<Long> getPermissionIds(Long roleId);

    /**
     * 移除角色的权限
     * @param roleId 角色ID
     * @param permissionId 权限ID
     */
    void removePermission(Long roleId, Long permissionId);

    /**
     * 查询拥有该角色的用户列表
     * @param roleId 角色ID
     * @return 用户ID列表
     */
    List<Long> getUserIds(Long roleId);
}
