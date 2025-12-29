package com.gsms.gsms.service;

import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.exception.CommonErrorCode;

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

    /**
     * 检查项目访问权限，无权限时抛出异常
     * @param userId 用户ID
     * @param projectId 项目ID
     * @throws BusinessException 无权限时抛出异常
     */
    default void checkProjectAccess(Long userId, Long projectId) {
        if (canViewAllProjects(userId)) {
            return;
        }

        List<Long> projectIds = getAccessibleProjectIds(userId);
        if (projectIds == null || !projectIds.contains(projectId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
    }

    /**
     * 检查工时访问权限，无权限时抛出异常
     * @param userId 用户ID
     * @param workHourUserId 工时记录所属用户ID
     * @throws BusinessException 无权限时抛出异常
     */
    default void checkWorkHourAccess(Long userId, Long workHourUserId) {
        if (canViewAllWorkHours(userId)) {
            return;
        }

        if (!userId.equals(workHourUserId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
    }
}
