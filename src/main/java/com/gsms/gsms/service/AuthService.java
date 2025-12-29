package com.gsms.gsms.service;

import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.exception.CommonErrorCode;

import java.util.List;

/**
 * 权限与数据范围服务接口
 * 提供用户权限判断和数据访问控制功能
 */
public interface AuthService {

    /**
     * 判断用户是否具备指定权限点
     *
     * @param userId 用户ID
     * @param permissionCode 权限代码
     * @return true表示有权限，false表示无权限
     */
    boolean hasPermission(Long userId, String permissionCode);

    /**
     * 判断用户是否可以查看所有项目
     *
     * @param userId 用户ID
     * @return true表示可以查看所有项目，false表示只能查看参与的项目
     */
    boolean canViewAllProjects(Long userId);

    /**
     * 判断用户是否可以查看所有任务
     *
     * @param userId 用户ID
     * @return true表示可以查看所有任务，false表示只能查看相关任务
     */
    boolean canViewAllTasks(Long userId);

    /**
     * 判断用户是否可以查看所有工时
     *
     * @param userId 用户ID
     * @return true表示可以查看所有工时，false表示只能查看自己的工时
     */
    boolean canViewAllWorkHours(Long userId);

    /**
     * 查询用户可访问的项目ID列表
     *
     * 基于用户参与的项目返回项目ID列表
     *
     * @param userId 用户ID
     * @return 项目ID列表，如果没有可访问项目返回空列表
     */
    List<Long> getAccessibleProjectIds(Long userId);

    /**
     * 检查项目访问权限
     *
     * 当用户无权限访问指定项目时抛出异常
     *
     * @param userId 用户ID
     * @param projectId 项目ID
     * @throws BusinessException 当用户无权限访问项目时抛出FORBIDDEN异常
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
     * 检查工时访问权限
     *
     * 当用户无权限访问指定工时记录时抛出异常
     *
     * @param userId 当前用户ID
     * @param workHourUserId 工时记录所属用户ID
     * @throws BusinessException 当用户无权限访问工时时抛出FORBIDDEN异常
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
