package com.gsms.gsms.service;

import com.gsms.gsms.dto.gantt.GanttDataResp;
import com.gsms.gsms.dto.gantt.TaskDateUpdateReq;
import com.gsms.gsms.dto.gantt.TaskLinkCreateReq;

import java.time.LocalDate;

/**
 * 甘特图服务接口
 */
public interface GanttService {

    /**
     * 获取项目甘特图数据
     *
     * @param projectId 项目ID
     * @param startDate 开始日期（可选）
     * @param endDate 结束日期（可选）
     * @return 甘特图数据（任务树形结构 + 依赖关系）
     */
    GanttDataResp getProjectGanttData(Long projectId, LocalDate startDate, LocalDate endDate);

    /**
     * 更新任务时间
     *
     * @param taskId 任务ID
     * @param req 任务时间更新请求
     */
    void updateTaskDates(Long taskId, TaskDateUpdateReq req);

    /**
     * 更新任务层级
     *
     * @param taskId 任务ID
     * @param newParentId 新的父任务ID（null表示顶级任务）
     */
    void updateTaskParent(Long taskId, Long newParentId);

    /**
     * 创建任务依赖关系
     *
     * @param req 任务依赖关系创建请求
     */
    void createTaskLink(TaskLinkCreateReq req);

    /**
     * 删除任务依赖关系
     *
     * @param linkId 依赖关系ID
     */
    void deleteTaskLink(Long linkId);
}
