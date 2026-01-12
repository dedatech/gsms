package com.gsms.gsms.dto.permission;

import com.gsms.gsms.model.entity.Permission;

/**
 * 权限实体与DTO转换器
 */
public class PermissionConverter {

    /**
     * 将 PermissionCreateReq 转换为 Permission 实体
     */
    public static Permission toEntity(PermissionCreateReq req) {
        if (req == null) {
            return null;
        }

        Permission permission = new Permission();
        permission.setName(req.getName());
        permission.setCode(req.getCode());
        permission.setDescription(req.getDescription());

        return permission;
    }

    /**
     * 将 PermissionUpdateReq 转换为 Permission 实体
     */
    public static Permission toEntity(PermissionUpdateReq req) {
        if (req == null) {
            return null;
        }

        Permission permission = new Permission();
        permission.setId(req.getId());
        permission.setName(req.getName());
        permission.setCode(req.getCode());
        permission.setDescription(req.getDescription());

        return permission;
    }
}
