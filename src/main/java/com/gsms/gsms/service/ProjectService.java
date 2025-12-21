package com.gsms.gsms.service;

import com.gsms.gsms.entity.Project;

import java.util.List;

/**
 * 项目服务接口
 */
public interface ProjectService {
    /**
     * 根据ID查询项目
     * @param id 项目ID
     * @return 项目实体
     */
    Project getProjectById(Long id);

    /**
     * 查询所有项目
     * @return 项目列表
     */
    List<Project> getAllProjects();

    /**
     * 根据条件查询项目
     * @param name 项目名称（模糊匹配）
     * @param status 项目状态
     * @return 项目列表
     */
    List<Project> getProjectsByCondition(String name, Integer status);

    /**
     * 创建项目
     * @param project 项目实体
     * @return 是否成功
     */
    boolean createProject(Project project);

    /**
     * 更新项目
     * @param project 项目实体
     * @return 是否成功
     */
    boolean updateProject(Project project);

    /**
     * 删除项目
     * @param id 项目ID
     * @return 是否成功
     */
    boolean deleteProject(Long id);
}