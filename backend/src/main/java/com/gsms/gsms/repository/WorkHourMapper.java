package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.WorkHour;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

/**
 * 工时记录Mapper接口
 */
@Mapper
public interface WorkHourMapper extends BaseMapper<WorkHour> {
    /**
     * 根据ID查询工时记录
     * @param id 工时记录ID
     * @return 工时记录实体
     */
    WorkHour selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询工时记录
     * @param userId 用户ID
     * @return 工时记录列表
     */
    List<WorkHour> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据项目ID查询工时记录
     * @param projectId 项目ID
     * @return 工时记录列表
     */
    List<WorkHour> selectByProjectId(@Param("projectId") Long projectId);

    /**
     * 根据任务ID查询工时记录
     * @param taskId 任务ID
     * @return 工时记录列表
     */
    List<WorkHour> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 根据条件查询工时记录
     * @param userId 用户ID
     * @param projectId 项目ID
     * @param taskId 任务ID
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 工时记录列表
     */
    List<WorkHour> selectByCondition(@Param("userId") Long userId, @Param("projectId") Long projectId, 
                                   @Param("taskId") Long taskId, @Param("startDate") LocalDate startDate,
                                   @Param("endDate") LocalDate endDate);

    /**
     * 插入工时记录
     * @param workHour 工时记录实体
     * @return 影响行数
     */
    int insert(WorkHour workHour);

    /**
     * 更新工时记录
     * @param workHour 工时记录实体
     * @return 影响行数
     */
    int update(WorkHour workHour);

    /**
     * 根据ID删除工时记录
     * @param id 工时记录ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询项目成员的总工时
     * @param projectId 项目ID
     * @param userId 用户ID
     * @return 总工时（小时）
     */
    Double selectTotalHoursByProjectIdAndUserId(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 查询项目成员在指定日期范围内的工时
     * @param projectId 项目ID
     * @param userId 用户ID
     * @param startDate 开始日期时间
     * @param endDate 结束日期时间
     * @return 总工时（小时）
     */
    Double selectTotalHoursByDateRange(@Param("projectId") Long projectId, @Param("userId") Long userId,
                                       @Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);
}