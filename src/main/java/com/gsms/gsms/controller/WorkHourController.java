package com.gsms.gsms.controller;

import com.gsms.gsms.dto.workhour.WorkHourConverter;
import com.gsms.gsms.dto.workhour.WorkHourCreateReq;
import com.gsms.gsms.dto.workhour.WorkHourQueryReq;
import com.gsms.gsms.dto.workhour.WorkHourUpdateReq;
import com.gsms.gsms.domain.entity.WorkHour;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.WorkHourService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 工时记录控制器
 */
@RestController
@RequestMapping("/api/work-hours")
@Tag(name = "工时管理接口", description = "工时记录相关的API接口")
public class WorkHourController {
    private static final Logger logger = LoggerFactory.getLogger(WorkHourController.class);

    @Autowired
    private WorkHourService workHourService;

    /**
     * 根据ID查询工时记录
     */
    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询工时记录")
    public Result<WorkHour> getWorkHourById(@PathVariable Long id) {
        logger.info("根据ID查询工时记录: {}", id);
        WorkHour workHour = workHourService.getWorkHourById(id);
        logger.info("成功查询到工时记录: ID={}", workHour.getId());
        return Result.success(workHour);
    }

    /**
     * 根据用户ID查询工时记录
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "根据用户ID查询工时记录")
    public Result<List<WorkHour>> getWorkHoursByUserId(@PathVariable Long userId) {
        logger.info("根据用户ID查询工时记录: {}", userId);
        List<WorkHour> workHours = workHourService.getWorkHoursByUserId(userId);
        logger.info("根据用户ID查询到{}条工时记录", workHours.size());
        return Result.success(workHours);
    }

    /**
     * 根据项目ID查询工时记录
     */
    @GetMapping("/project/{projectId}")
    @Operation(summary = "根据项目ID查询工时记录")
    public Result<List<WorkHour>> getWorkHoursByProjectId(@PathVariable Long projectId) {
        logger.info("根据项目ID查询工时记录: {}", projectId);
        List<WorkHour> workHours = workHourService.getWorkHoursByProjectId(projectId);
        logger.info("根据项目ID查询到{}条工时记录", workHours.size());
        return Result.success(workHours);
    }

    /**
     * 根据条件查询工时记录
     */
    @GetMapping("/search")
    @Operation(summary = "根据条件查询工时记录")
    public Result<List<WorkHour>> getWorkHoursByCondition(WorkHourQueryReq req) {
        logger.info("根据条件查询工时记录: userId={}, projectId={}, taskId={}, startDate={}, endDate={}", 
                    req.getUserId(), req.getProjectId(), req.getTaskId(), req.getStartDate(), req.getEndDate());
        List<WorkHour> workHours = workHourService.getWorkHoursByCondition(
                req.getUserId(), req.getProjectId(), req.getTaskId(), req.getStartDate(), req.getEndDate());
        logger.info("根据条件查询到{}条工时记录", workHours.size());
        return Result.success(workHours);
    }

    /**
     * 创建工时记录
     */
    @PostMapping
    @Operation(summary = "创建工时记录")
    public Result<WorkHour> createWorkHour(@RequestBody @Valid WorkHourCreateReq req) {
        logger.info("创建工时记录: projectId={}, taskId={}", req.getProjectId(), req.getTaskId());
        WorkHour workHour = WorkHourConverter.toWorkHour(req);
        WorkHour createdWorkHour = workHourService.createWorkHour(workHour);
        logger.info("工时记录创建成功: ID={}", createdWorkHour.getId());
        return Result.success(createdWorkHour);
    }

    /**
     * 更新工时记录
     */
    @PutMapping
    @Operation(summary = "更新工时记录")
    public Result<WorkHour> updateWorkHour(@RequestBody @Valid WorkHourUpdateReq req) {
        logger.info("更新工时记录: {}", req.getId());
        WorkHour workHour = WorkHourConverter.toWorkHour(req);
        WorkHour updatedWorkHour = workHourService.updateWorkHour(workHour);
        logger.info("工时记录更新成功: {}", updatedWorkHour.getId());
        return Result.success(updatedWorkHour);
    }

    /**
     * 删除工时记录
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除工时记录")
    public Result<String> deleteWorkHour(@PathVariable Long id) {
        logger.info("删除工时记录: {}", id);
        workHourService.deleteWorkHour(id);
        logger.info("工时记录删除成功: {}", id);
        return Result.success("工时记录删除成功");
    }
}