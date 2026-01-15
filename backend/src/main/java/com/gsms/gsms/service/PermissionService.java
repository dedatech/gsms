package com.gsms.gsms.service;

import com.gsms.gsms.dto.permission.PermissionCreateReq;
import com.gsms.gsms.dto.permission.PermissionInfoResp;
import com.gsms.gsms.dto.permission.PermissionQueryReq;
import com.gsms.gsms.dto.permission.PermissionUpdateReq;
import com.gsms.gsms.infra.common.PageResult;

import java.util.List;

/**
 * 权限服务接口
 */
public interface PermissionService {

    /**
     * 根据ID查询权限
     * @param id 权限ID
     * @return 权限响应DTO
     */
    PermissionInfoResp getById(Long id);

    /**
     * 根据条件分页查询权限
     * @param req 查询条件
     * @return 分页结果
     */
    PageResult<PermissionInfoResp> findAll(PermissionQueryReq req);

    /**
     * 获取所有权限（不分页，用于角色分配）
     * @return 权限响应DTO列表
     */
    List<PermissionInfoResp> getAll();

    /**
     * 创建权限
     * @param createReq 创建请求DTO
     * @return 创建成功的权限响应DTO
     */
    PermissionInfoResp create(PermissionCreateReq createReq);

    /**
     * 更新权限
     * @param updateReq 更新请求DTO
     * @return 更新后的权限响应DTO
     */
    PermissionInfoResp update(PermissionUpdateReq updateReq);

    /**
     * 删除权限
     * @param id 权限ID
     */
    void delete(Long id);

    /**
     * 为权限分配菜单
     * @param permissionId 权限ID
     * @param menuIds 菜单ID列表
     */
    void assignMenus(Long permissionId, List<Long> menuIds);

    /**
     * 获取权限的菜单ID列表
     * @param permissionId 权限ID
     * @return 菜单ID列表
     */
    List<Long> getMenuIds(Long permissionId);
}
