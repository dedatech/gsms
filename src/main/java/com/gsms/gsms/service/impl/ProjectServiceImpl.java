package com.gsms.gsms.service.impl;

import com.gsms.gsms.entity.Project;
import com.gsms.gsms.mapper.ProjectMapper;
import com.gsms.gsms.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 项目服务实现类
 */
@Service
public class ProjectServiceImpl implements ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Override
    public Project getProjectById(Long id) {
        return projectMapper.selectById(id);
    }

    @Override
    public List<Project> getAllProjects() {
        return projectMapper.selectAll();
    }

    @Override
    public List<Project> getProjectsByCondition(String name, Integer status) {
        return projectMapper.selectByCondition(name, status);
    }

    @Override
    public boolean createProject(Project project) {
        return projectMapper.insert(project) > 0;
    }

    @Override
    public boolean updateProject(Project project) {
        return projectMapper.update(project) > 0;
    }

    @Override
    public boolean deleteProject(Long id) {
        return projectMapper.deleteById(id) > 0;
    }
}