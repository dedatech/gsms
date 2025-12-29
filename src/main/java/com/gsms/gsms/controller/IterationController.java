package com.gsms.gsms.controller;

import com.gsms.gsms.dto.iteration.IterationCreateReq;
import com.gsms.gsms.dto.iteration.IterationQueryReq;
import com.gsms.gsms.dto.iteration.IterationUpdateReq;
import com.gsms.gsms.dto.iteration.IterationInfoResp;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.service.IterationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Tag(name = "迭代管理", description = "迭代管理相关接口")
@RestController
@RequestMapping("/api/iterations")
public class IterationController {
    private static final Logger logger = LoggerFactory.getLogger(IterationController.class);

    private final IterationService iterationService;

    public IterationController(IterationService iterationService) {
        this.iterationService = iterationService;
    }

    @Operation(summary = "根据ID获取迭代")
    @GetMapping("/{id}")
    public Result<IterationInfoResp> getById(
            @Parameter(description = "迭代ID", required = true) @PathVariable Long id) {
        logger.info("根据ID查询迭代: {}", id);
        IterationInfoResp iteration = iterationService.getById(id);
        logger.info("成功查询到迭代: {}", iteration.getName());
        return Result.success(iteration);
    }

    @Operation(summary = "根据条件分页查询迭代")
    @PostMapping("/query")
    public PageResult<IterationInfoResp> findAll(@RequestBody IterationQueryReq req) {
        logger.info("根据条件分页查询迭代: projectId={}, status={}, pageNum={}, pageSize={}",
                req.getProjectId(), req.getStatus(), req.getPageNum(), req.getPageSize());
        return iterationService.findAll(req);
    }

    @Operation(summary = "创建迭代")
    @PostMapping
    public Result<IterationInfoResp> create(@Validated @RequestBody IterationCreateReq req) {
        logger.info("创建迭代: {}", req.getName());
        IterationInfoResp createdIteration = iterationService.create(req);
        logger.info("迭代创建成功: {}", createdIteration.getId());
        return Result.success(createdIteration);
    }

    @Operation(summary = "更新迭代")
    @PutMapping
    public Result<IterationInfoResp> update(@Validated @RequestBody IterationUpdateReq req) {
        logger.info("更新迭代: {}", req.getId());
        IterationInfoResp updatedIteration = iterationService.update(req);
        logger.info("迭代更新成功: {}", updatedIteration.getId());
        return Result.success(updatedIteration);
    }

    @Operation(summary = "删除迭代")
    @DeleteMapping("/{id}")
    public Result<String> delete(
            @Parameter(description = "迭代ID", required = true) @PathVariable Long id) {
        logger.info("删除迭代: {}", id);
        iterationService.delete(id);
        logger.info("迭代删除成功: {}", id);
        return Result.success("迭代删除成功");
    }
}