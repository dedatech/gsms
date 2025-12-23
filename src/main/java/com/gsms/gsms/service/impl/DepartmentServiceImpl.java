package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.Department;
import com.gsms.gsms.domain.enums.errorcode.DepartmentErrorCode;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.repository.DepartmentMapper;
import com.gsms.gsms.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Override
    public Department getDepartmentById(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND);
        }
        return department;
    }

    @Override
    public List<Department> getAllDepartments() {
        return departmentMapper.selectAll();
    }

    @Override
    public List<Department> getDepartmentsByParentId(Long parentId) {
        return departmentMapper.selectByParentId(parentId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Department createDepartment(Department department) {
        int result = departmentMapper.insert(department);
        if (result <= 0) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_CREATE_FAILED);
        }
        return department;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Department updateDepartment(Department department) {
        Department existDepartment = departmentMapper.selectById(department.getId());
        if (existDepartment == null) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND);
        }
        
        int result = departmentMapper.update(department);
        if (result <= 0) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_UPDATE_FAILED);
        }
        
        return departmentMapper.selectById(department.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDepartment(Long id) {
        Department existDepartment = departmentMapper.selectById(id);
        if (existDepartment == null) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND);
        }
        
        int result = departmentMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_DELETE_FAILED);
        }
    }
}
