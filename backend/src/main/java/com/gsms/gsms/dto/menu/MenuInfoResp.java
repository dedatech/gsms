package com.gsms.gsms.dto.menu;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gsms.gsms.model.entity.Menu;
import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单信息响应
 */
@Schema(description = "菜单信息响应")
public class MenuInfoResp {

    @Schema(description = "菜单ID")
    private Long id;

    @Schema(description = "菜单名称")
    private String name;

    @Schema(description = "路由路径")
    private String path;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "菜单图标")
    private String icon;

    @Schema(description = "父菜单ID")
    private Long parentId;

    @Schema(description = "排序号")
    private Integer sort;

    @Schema(description = "菜单类型 1:目录 2:菜单 3:按钮")
    private Integer type;

    @Schema(description = "状态 1:启用 2:禁用")
    private Integer status;

    @Schema(description = "是否可见 1:可见 2:隐藏")
    private Integer visible;

    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;

    @Schema(description = "子菜单列表（树形结构）")
    private List<MenuInfoResp> children;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getComponent() {
        return component;
    }

    public void setComponent(String component) {
        this.component = component;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getVisible() {
        return visible;
    }

    public void setVisible(Integer visible) {
        this.visible = visible;
    }

    public List<Long> getPermissionIds() {
        return permissionIds;
    }

    public void setPermissionIds(List<Long> permissionIds) {
        this.permissionIds = permissionIds;
    }

    public List<MenuInfoResp> getChildren() {
        return children;
    }

    public void setChildren(List<MenuInfoResp> children) {
        this.children = children;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    /**
     * 将 Menu 实体转换为 MenuInfoResp
     */
    public static MenuInfoResp from(Menu menu) {
        if (menu == null) {
            return null;
        }

        MenuInfoResp resp = new MenuInfoResp();
        resp.setId(menu.getId());
        resp.setName(menu.getName());
        resp.setPath(menu.getPath());
        resp.setComponent(menu.getComponent());
        resp.setIcon(menu.getIcon());
        resp.setParentId(menu.getParentId());
        resp.setSort(menu.getSort());
        resp.setType(menu.getType() != null ? menu.getType().getCode() : null);
        resp.setStatus(menu.getStatus() != null ? menu.getStatus().getCode() : null);
        resp.setVisible(menu.getVisible());
        resp.setCreateTime(menu.getCreateTime());
        resp.setUpdateTime(menu.getUpdateTime());

        return resp;
    }

    /**
     * 将 Menu 列表转换为 MenuInfoResp 列表
     */
    public static List<MenuInfoResp> from(List<Menu> menus) {
        if (menus == null) {
            return java.util.Collections.emptyList();
        }

        return menus.stream()
                .map(MenuInfoResp::from)
                .collect(Collectors.toList());
    }
}
