package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Department;

import java.util.List;

public interface DepartmentService {
    Department getDepartmentById(Long id);

    List<Department> getAllDepartments();

    List<Department> getDepartmentsByParentId(Long parentId);

    Department createDepartment(Department department);

    Department updateDepartment(Department department);

    void deleteDepartment(Long id);
}
