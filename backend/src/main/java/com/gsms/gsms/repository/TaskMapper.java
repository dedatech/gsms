package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 任务Mapper接口
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    /**
     * 根据ID查询任务
     * @param id 任务ID
     * @return 任务实体
     */
    Task selectById(@Param("id") Long id);

    /**
     * 根据ID查询任务（仅限用户可访问的项目）
     * @param id 任务ID
     * @param userId 用户ID
     * @return 任务实体
     */
    Task selectByIdForUser(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 根据项目ID查询任务
     * @param projectId 项目ID
     * @return 任务列表
     */
    List<Task> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据条件查询任务
     * @param projectId 项目 ID
     * @param assigneeId 负责人 ID
     * @param status 任务状态
     * @return 任务列表
     */
    List<Task> selectByCondition(@Param("projectId") Long projectId, @Param("assigneeId") Long assigneeId, @Param("status") Integer status);
    
    /**
     * 根据条件查询用户可访问的任务（基于项目成员表）
     * @param userId 用户ID
     * @param projectId 项目 ID
     * @param assigneeId 负责人 ID
     * @param status 任务状态
     * @return 任务列表
     */
    List<Task> selectAccessibleTasksByCondition(@Param("userId") Long userId, @Param("projectId") Long projectId, @Param("assigneeId") Long assigneeId, @Param("status") Integer status);

    /**
     * 插入任务
     * @param task 任务实体
     * @return 影响行数
     */
    int insert(Task task);

    /**
     * 更新任务
     * @param task 任务实体
     * @return 影响行数
     */
    int update(Task task);

    /**
     * 更新任务状态（支持将 actualStartDate 和 actualEndDate 更新为 null）
     * @param task 任务实体
     * @return 影响行数
     */
    int updateStatus(Task task);

    /**
     * 根据ID删除任务
     * @param id 任务ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}