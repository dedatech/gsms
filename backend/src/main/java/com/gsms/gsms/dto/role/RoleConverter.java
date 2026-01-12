package com.gsms.gsms.dto.role;

import com.gsms.gsms.model.entity.Role;

/**
 * 角色实体与DTO转换器
 */
public class RoleConverter {

    /**
     * 将 RoleCreateReq 转换为 Role 实体
     */
    public static Role toEntity(RoleCreateReq req) {
        if (req == null) {
            return null;
        }

        Role role = new Role();
        role.setName(req.getName());
        role.setCode(req.getCode());
        role.setDescription(req.getDescription());
        role.setRoleLevel(req.getRoleLevel());

        return role;
    }

    /**
     * 将 RoleUpdateReq 转换为 Role 实体
     */
    public static Role toEntity(RoleUpdateReq req) {
        if (req == null) {
            return null;
        }

        Role role = new Role();
        role.setId(req.getId());
        role.setName(req.getName());
        role.setCode(req.getCode());
        role.setDescription(req.getDescription());
        role.setRoleLevel(req.getRoleLevel());

        return role;
    }
}
