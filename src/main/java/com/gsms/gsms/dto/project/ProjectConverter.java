package com.gsms.gsms.dto.project;

import com.gsms.gsms.domain.entity.Project;
import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.dto.project.ProjectUpdateReq;

/**
 * 项目对象转换器
 */
public class ProjectConverter {
    
    /**
     * 创建请求转项目实体
     */
    public static Project toProject(ProjectCreateReq req) {
        if (req == null) {
            return null;
        }
        Project project = new Project();
        project.setName(req.getName());
        project.setCode(req.getCode());
        project.setDescription(req.getDescription());
        project.setManagerId(req.getManagerId());
        project.setStatus(req.getStatus());  // 直接设置枚举，MyBatis-Plus自动转换
        project.setPlanStartDate(req.getPlanStartDate());
        project.setPlanEndDate(req.getPlanEndDate());
        return project;
    }
    
    /**
     * 更新请求转项目实体
     */
    public static Project toProject(ProjectUpdateReq req) {
        if (req == null) {
            return null;
        }
        Project project = new Project();
        project.setId(req.getId());
        project.setName(req.getName());
        project.setCode(req.getCode());
        project.setDescription(req.getDescription());
        project.setManagerId(req.getManagerId());
        project.setStatus(req.getStatus());  // 直接设置枚举，MyBatis-Plus自动转换
        project.setPlanStartDate(req.getPlanStartDate());
        project.setPlanEndDate(req.getPlanEndDate());
        project.setActualStartDate(req.getActualStartDate());
        project.setActualEndDate(req.getActualEndDate());
        return project;
    }
}
