package com.gsms.gsms.controller;

import com.gsms.gsms.domain.entity.Iteration;
import com.gsms.gsms.dto.iteration.IterationConverter;
import com.gsms.gsms.dto.iteration.IterationCreateReq;
import com.gsms.gsms.dto.iteration.IterationQueryReq;
import com.gsms.gsms.dto.iteration.IterationUpdateReq;
import com.gsms.gsms.infra.common.Result;
import com.gsms.gsms.service.IterationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "迭代管理", description = "迭代管理相关接口")
@RestController
@RequestMapping("/api/iterations")
public class IterationController {

    @Autowired
    private IterationService iterationService;

    @Operation(summary = "根据ID获取迭代")
    @GetMapping("/{id}")
    public Result<Iteration> getIterationById(
            @Parameter(description = "迭代ID", required = true) @PathVariable Long id) {
        Iteration iteration = iterationService.getIterationById(id);
        return Result.success(iteration);
    }

    @Operation(summary = "根据项目ID获取迭代列表")
    @GetMapping("/project/{projectId}")
    public Result<List<Iteration>> getIterationsByProjectId(
            @Parameter(description = "项目ID", required = true) @PathVariable Long projectId) {
        List<Iteration> iterations = iterationService.getIterationsByProjectId(projectId);
        return Result.success(iterations);
    }

    @Operation(summary = "查询迭代列表")
    @PostMapping("/query")
    public Result<List<Iteration>> queryIterations(@RequestBody IterationQueryReq req) {
        List<Iteration> iterations = iterationService.getIterationsByCondition(
                req.getProjectId(),
                req.getStatus() != null ? req.getStatus().getCode() : null
        );
        return Result.success(iterations);
    }

    @Operation(summary = "创建迭代")
    @PostMapping
    public Result<Iteration> createIteration(@Validated @RequestBody IterationCreateReq req) {
        Iteration iteration = IterationConverter.toEntity(req);
        Iteration createdIteration = iterationService.createIteration(iteration);
        return Result.success(createdIteration);
    }

    @Operation(summary = "更新迭代")
    @PutMapping
    public Result<Iteration> updateIteration(@Validated @RequestBody IterationUpdateReq req) {
        Iteration iteration = IterationConverter.toEntity(req);
        Iteration updatedIteration = iterationService.updateIteration(iteration);
        return Result.success(updatedIteration);
    }

    @Operation(summary = "删除迭代")
    @DeleteMapping("/{id}")
    public Result<Void> deleteIteration(
            @Parameter(description = "迭代ID", required = true) @PathVariable Long id) {
        iterationService.deleteIteration(id);
        return Result.success();
    }
}
