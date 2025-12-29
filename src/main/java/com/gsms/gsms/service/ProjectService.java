package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.dto.project.ProjectQueryReq;
import com.gsms.gsms.infra.common.PageResult;

/**
 * 项目服务接口
 */
public interface ProjectService {
    /**
     * 根据ID查询项目
     * @param id 项目ID
     * @return 项目实体
     */
    Project getById(Long id);

    /**
     * 根据条件分页查询项目
     * @param req 查询条件
     * @return 分页结果
     */
    PageResult<Project> findAll(ProjectQueryReq req);

    /**
     * 创建项目
     * @param project 项目实体
     * @return 创建成功的项目实体
     */
    Project create(Project project);

    /**
     * 更新项目
     * @param project 项目实体
     * @return 更新后的项目实体
     */
    Project update(Project project);

    /**
     * 删除项目
     * @param id 项目ID
     */
    void delete(Long id);
}