package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.dto.menu.*;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.model.entity.Menu;
import com.gsms.gsms.model.enums.errorcode.MenuErrorCode;
import com.gsms.gsms.repository.MenuMapper;
import com.gsms.gsms.service.MenuService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单服务实现类
 */
@Service
public class MenuServiceImpl implements MenuService {

    private static final Logger logger = LoggerFactory.getLogger(MenuServiceImpl.class);

    private final MenuMapper menuMapper;

    public MenuServiceImpl(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    @Override
    public MenuInfoResp getById(Long id) {
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(MenuErrorCode.MENU_NOT_FOUND);
        }

        MenuInfoResp resp = MenuInfoResp.from(menu);
        resp.setPermissionIds(menuMapper.selectPermissionIdsByMenuId(id));
        return resp;
    }

    @Override
    public PageResult<MenuInfoResp> findAll(MenuQueryReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<Menu> list = menuMapper.selectByCondition(
                req.getName(), req.getType(), req.getStatus(), req.getParentId());
        PageInfo<Menu> pageInfo = new PageInfo<>(list);

        List<MenuInfoResp> respList = MenuInfoResp.from(list);
        return PageResult.success(respList, pageInfo.getTotal(),
                                 pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    public List<MenuInfoResp> getTree() {
        List<Menu> allMenus = menuMapper.selectAll();
        return buildMenuTree(allMenus, 0L);
    }

    @Override
    public List<MenuInfoResp> getUserMenuTree() {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            logger.warn("用户未登录，返回空菜单树");
            return new ArrayList<>();
        }

        List<Menu> userMenus = menuMapper.selectMenusByUserId(currentUserId);
        logger.info("用户 {} 可访问菜单数量: {}", currentUserId, userMenus.size());

        return buildMenuTree(userMenus, 0L);
    }

    /**
     * 构建菜单树（递归）
     */
    private List<MenuInfoResp> buildMenuTree(List<Menu> menus, Long parentId) {
        List<MenuInfoResp> tree = new ArrayList<>();

        for (Menu menu : menus) {
            if (menu.getParentId().equals(parentId)) {
                MenuInfoResp node = MenuInfoResp.from(menu);

                // 递归查找子菜单
                List<MenuInfoResp> children = buildMenuTree(menus, menu.getId());
                if (!children.isEmpty()) {
                    node.setChildren(children);
                }

                tree.add(node);
            }
        }

        return tree;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuInfoResp create(MenuCreateReq req) {
        // 验证父菜单是否存在
        if (!req.getParentId().equals(0L)) {
            Menu parentMenu = menuMapper.selectById(req.getParentId());
            if (parentMenu == null) {
                throw new BusinessException(MenuErrorCode.PARENT_MENU_NOT_FOUND);
            }
        }

        // DTO转Entity
        Menu menu = MenuConverter.toEntity(req);

        // 设置审计字段
        Long currentUserId = UserContext.getCurrentUserId();
        menu.setCreateUserId(currentUserId != null ? currentUserId : 1L);
        menu.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = menuMapper.insert(menu);
        if (result <= 0) {
            throw new BusinessException(MenuErrorCode.MENU_CREATE_FAILED);
        }

        // 分配权限
        if (req.getPermissionIds() != null && !req.getPermissionIds().isEmpty()) {
            assignPermissions(menu.getId(), req.getPermissionIds());
        }

        logger.info("菜单创建成功: id={}, name={}", menu.getId(), menu.getName());
        return MenuInfoResp.from(menu);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MenuInfoResp update(MenuUpdateReq req) {
        // 检查菜单是否存在
        Menu existingMenu = menuMapper.selectById(req.getId());
        if (existingMenu == null) {
            throw new BusinessException(MenuErrorCode.MENU_NOT_FOUND);
        }

        // 验证父菜单
        if (req.getParentId() != null && !req.getParentId().equals(0L)) {
            if (req.getParentId().equals(req.getId())) {
                throw new BusinessException(MenuErrorCode.PARENT_MENU_CANNOT_BE_SELF);
            }
            Menu parentMenu = menuMapper.selectById(req.getParentId());
            if (parentMenu == null) {
                throw new BusinessException(MenuErrorCode.PARENT_MENU_NOT_FOUND);
            }
        }

        // DTO转Entity
        Menu menu = MenuConverter.toEntity(req);
        Long currentUserId = UserContext.getCurrentUserId();
        menu.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = menuMapper.update(menu);
        if (result <= 0) {
            throw new BusinessException(MenuErrorCode.MENU_UPDATE_FAILED);
        }

        // 更新权限关联（只有在明确提供了权限列表时才更新）
        if (req.getPermissionIds() != null && !req.getPermissionIds().isEmpty()) {
            assignPermissions(req.getId(), req.getPermissionIds());
        }

        logger.info("菜单更新成功: id={}", req.getId());
        return getById(req.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        // 检查菜单是否存在
        Menu menu = menuMapper.selectById(id);
        if (menu == null) {
            throw new BusinessException(MenuErrorCode.MENU_NOT_FOUND);
        }

        // 检查是否有子菜单
        List<Menu> children = menuMapper.selectByParentId(id);
        if (!children.isEmpty()) {
            throw new BusinessException(MenuErrorCode.MENU_HAS_CHILDREN);
        }

        int result = menuMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(MenuErrorCode.MENU_DELETE_FAILED);
        }

        // 删除权限关联
        menuMapper.deletePermissionsByMenuId(id);

        logger.info("菜单删除成功: id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long menuId, List<Long> permissionIds) {
        // 删除现有权限关联
        menuMapper.deletePermissionsByMenuId(menuId);

        // 插入新的权限关联
        if (permissionIds != null && !permissionIds.isEmpty()) {
            Long currentUserId = UserContext.getCurrentUserId();
            int result = menuMapper.insertMenuPermissions(
                menuId, permissionIds, currentUserId != null ? currentUserId : 1L);
            logger.info("菜单权限分配成功: menuId={}, count={}", menuId, result);
        }
    }

    @Override
    public List<Long> getPermissionIds(Long menuId) {
        return menuMapper.selectPermissionIdsByMenuId(menuId);
    }
}
