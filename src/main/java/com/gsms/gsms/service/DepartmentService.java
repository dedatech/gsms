package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Department;
import com.gsms.gsms.dto.department.DepartmentQueryReq;
import com.gsms.gsms.infra.common.PageResult;

import java.util.List;

public interface DepartmentService {
    Department getDepartmentById(Long id);

    List<Department> getAllDepartments();

    List<Department> getDepartmentsByParentId(Long parentId);
    
    List<Department> getDepartmentsByCondition(String name, Long parentId);

    Department createDepartment(Department department);

    Department updateDepartment(Department department);

    void deleteDepartment(Long id);
}