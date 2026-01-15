package com.gsms.gsms.service;

import com.gsms.gsms.dto.menu.*;
import com.gsms.gsms.infra.common.PageResult;

import java.util.List;

/**
 * 菜单服务接口
 */
public interface MenuService {

    /**
     * 根据ID查询菜单
     */
    MenuInfoResp getById(Long id);

    /**
     * 根据条件分页查询菜单
     */
    PageResult<MenuInfoResp> findAll(MenuQueryReq req);

    /**
     * 查询所有菜单（树形结构）
     */
    List<MenuInfoResp> getTree();

    /**
     * 根据当前用户查询可访问菜单（树形结构）
     */
    List<MenuInfoResp> getUserMenuTree();

    /**
     * 创建菜单
     */
    MenuInfoResp create(MenuCreateReq req);

    /**
     * 更新菜单
     */
    MenuInfoResp update(MenuUpdateReq req);

    /**
     * 删除菜单
     */
    void delete(Long id);

    /**
     * 为菜单分配权限
     */
    void assignPermissions(Long menuId, List<Long> permissionIds);

    /**
     * 查询菜单的权限ID列表
     */
    List<Long> getPermissionIds(Long menuId);
}
