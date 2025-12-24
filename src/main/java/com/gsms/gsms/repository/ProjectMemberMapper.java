package com.gsms.gsms.repository;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目成员 Mapper
 */
@Mapper
public interface ProjectMemberMapper {

    /**
     * 根据用户ID查询其参与的项目ID列表
     */
    List<Long> selectProjectIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据项目ID查询项目成员的用户ID列表
     */
    List<Long> selectUserIdsByProjectId(@Param("projectId") Long projectId);

    /**
     * 插入项目成员
     */
    int insertProjectMember(@Param("projectId") Long projectId,
                            @Param("userId") Long userId,
                            @Param("roleType") Integer roleType,
                            @Param("createUserId") Long createUserId);
}
