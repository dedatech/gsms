package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.ProjectMember;

import java.util.List;

/**
 * 项目成员服务
 */
public interface ProjectMemberService {

    /**
     * 查询指定项目的成员列表
     */
    List<ProjectMember> listMembersByProjectId(Long projectId);

    /**
     * 为项目批量添加成员
     */
    void addMembers(Long projectId, List<Long> userIds, Integer roleType);

    /**
     * 更新项目成员角色
     */
    void updateMemberRole(Long projectId, Long userId, Integer roleType);

    /**
     * 从项目中移除成员
     */
    void removeMember(Long projectId, Long userId);
}
