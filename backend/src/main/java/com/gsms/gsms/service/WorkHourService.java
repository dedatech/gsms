package com.gsms.gsms.service;

import com.gsms.gsms.model.entity.WorkHour;

import java.util.Date;
import java.util.List;

/**
 * 工时记录服务接口
 */
public interface WorkHourService {
    /**
     * 根据ID查询工时记录
     * @param id 工时记录ID
     * @return 工时记录实体
     */
    WorkHour getWorkHourById(Long id);

    /**
     * 根据用户ID查询工时记录
     * @param userId 用户ID
     * @return 工时记录列表
     */
    List<WorkHour> getWorkHoursByUserId(Long userId);

    /**
     * 根据项目ID查询工时记录
     * @param projectId 项目ID
     * @return 工时记录列表
     */
    List<WorkHour> getWorkHoursByProjectId(Long projectId);

    /**
     * 根据条件查询工时记录
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param taskId 任务ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工时记录列表
     */
    List<WorkHour> getWorkHoursByCondition(Long userId, Long projectId, Long taskId, Date startDate, Date endDate);

    /**
     * 创建工时记录
     * @param workHour 工时记录实体
     * @return 创建成功的工时记录实体
     * @throws BusinessException 工时信息无效时抛出异常
     */
    WorkHour createWorkHour(WorkHour workHour);

    /**
     * 更新工时记录
     * @param workHour 工时记录实体
     * @return 更新后的工时记录实体
     * @throws BusinessException 工时记录不存在时抛出异常
     */
    WorkHour updateWorkHour(WorkHour workHour);

    /**
     * 删除工时记录
     * @param id 工时记录ID
     * @throws BusinessException 工时记录不存在时抛出异常
     */
    void deleteWorkHour(Long id);
}