package com.gsms.gsms.dto.menu;

import com.gsms.gsms.model.entity.Menu;
import com.gsms.gsms.model.enums.MenuStatus;
import com.gsms.gsms.model.enums.MenuType;

/**
 * 菜单 DTO 转换器
 */
public class MenuConverter {

    /**
     * 创建请求转实体
     */
    public static Menu toEntity(MenuCreateReq req) {
        if (req == null) {
            return null;
        }

        Menu menu = new Menu();
        menu.setName(req.getName());
        menu.setPath(req.getPath());
        menu.setComponent(req.getComponent());
        menu.setIcon(req.getIcon());
        menu.setParentId(req.getParentId());
        menu.setSort(req.getSort() != null ? req.getSort() : 0);
        menu.setType(MenuType.fromCode(req.getType()));
        menu.setStatus(req.getStatus() != null ? MenuStatus.fromCode(req.getStatus()) : MenuStatus.ENABLED);
        menu.setVisible(req.getVisible() != null ? req.getVisible() : 1);

        return menu;
    }

    /**
     * 更新请求转实体
     */
    public static Menu toEntity(MenuUpdateReq req) {
        if (req == null) {
            return null;
        }

        Menu menu = new Menu();
        menu.setId(req.getId());
        menu.setName(req.getName());
        menu.setPath(req.getPath());
        menu.setComponent(req.getComponent());
        menu.setIcon(req.getIcon());
        menu.setParentId(req.getParentId());
        menu.setSort(req.getSort());
        if (req.getType() != null) {
            menu.setType(MenuType.fromCode(req.getType()));
        }
        if (req.getStatus() != null) {
            menu.setStatus(MenuStatus.fromCode(req.getStatus()));
        }
        menu.setVisible(req.getVisible());

        return menu;
    }
}
