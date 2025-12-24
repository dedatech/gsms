package com.gsms.gsms.service;

import java.util.List;

/**
 * 权限与数据范围相关服务
 */
public interface AuthService {

    /**
     * 是否具备指定权限点
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 是否可以查看所有项目
     */
    boolean canViewAllProjects(Long userId);

    /**
     * 是否可以查看所有任务
     */
    boolean canViewAllTasks(Long userId);

    /**
     * 是否可以查看所有工时
     */
    boolean canViewAllWorkHours(Long userId);

    /**
     * 查询用户可访问的项目ID列表（基于项目成员）
     */
    List<Long> getAccessibleProjectIds(Long userId);
}
