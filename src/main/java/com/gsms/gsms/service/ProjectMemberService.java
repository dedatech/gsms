package com.gsms.gsms.service;

import com.gsms.gsms.model.entity.ProjectMember;
import com.gsms.gsms.model.enums.ProjectMemberRole;

import java.util.List;

/**
 * 项目成员服务接口
 * 提供项目成员的查询、添加、角色管理等功能
 */
public interface ProjectMemberService {

    /**
     * 查询指定项目的成员列表
     *
     * 根据项目ID查询该项目的所有成员信息，仅返回当前用户有权查看的成员
     *
     * @param projectId 项目ID
     * @return 项目成员列表
     */
    List<ProjectMember> listMembersByProjectId(Long projectId);

    /**
     * 为项目批量添加成员
     *
     * 将多个用户添加到项目中，并指定角色类型。自动过滤已存在的成员
     *
     * @param projectId 项目ID
     * @param userIds 用户ID列表
     * @param roleType 角色类型（1=项目经理，2=普通成员）
     * @throws BusinessException 当项目不存在或用户无权限时抛出
     */
    void addMembers(Long projectId, List<Long> userIds, Integer roleType);

    /**
     * 更新项目成员角色
     *
     * 修改指定用户在项目中的角色类型
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param roleType 新的角色类型（1=项目经理，2=普通成员）
     * @throws BusinessException 当成员不存在或用户无权限时抛出
     */
    void updateMemberRole(Long projectId, Long userId, Integer roleType);

    /**
     * 从项目中移除成员
     *
     * 将指定用户从项目中移除
     *
     * @param projectId 项目ID
     * @param userId 用户ID
     * @throws BusinessException 当成员不存在或用户无权限时抛出
     */
    void removeMember(Long projectId, Long userId);
}
