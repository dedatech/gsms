package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 菜单 Mapper
 */
@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    /**
     * 根据ID查询菜单
     */
    Menu selectById(@Param("id") Long id);

    /**
     * 根据条件查询菜单列表
     */
    List<Menu> selectByCondition(@Param("name") String name,
                                  @Param("type") Integer type,
                                  @Param("status") Integer status,
                                  @Param("parentId") Long parentId);

    /**
     * 查询所有菜单
     */
    List<Menu> selectAll();

    /**
     * 查询根菜单（parent_id = 0）
     */
    List<Menu> selectRootMenus();

    /**
     * 根据父菜单ID查询子菜单
     */
    List<Menu> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 插入菜单
     */
    int insert(Menu menu);

    /**
     * 更新菜单
     */
    int update(Menu menu);

    /**
     * 根据ID删除菜单（软删除）
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询菜单的权限ID列表
     */
    List<Long> selectPermissionIdsByMenuId(@Param("menuId") Long menuId);

    /**
     * 删除菜单的所有权限
     */
    int deletePermissionsByMenuId(@Param("menuId") Long menuId);

    /**
     * 批量插入菜单权限关联
     */
    int insertMenuPermissions(@Param("menuId") Long menuId, @Param("permissionIds") List<Long> permissionIds, @Param("updateUserId") Long updateUserId);

    /**
     * 根据用户ID查询可访问的菜单列表（树形结构）
     */
    List<Menu> selectMenusByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询可访问的菜单列表
     */
    List<Menu> selectMenusByRoleId(@Param("roleId") Long roleId);
}
