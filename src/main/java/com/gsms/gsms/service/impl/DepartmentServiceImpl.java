package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.domain.entity.Department;
import com.gsms.gsms.domain.enums.errorcode.DepartmentErrorCode;
import com.gsms.gsms.dto.department.DepartmentQueryReq;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.repository.DepartmentMapper;
import com.gsms.gsms.service.DepartmentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 部门服务实现类
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {

    private final DepartmentMapper departmentMapper;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper) {
        this.departmentMapper = departmentMapper;
    }

    @Override
    public Department getById(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND);
        }
        return department;
    }

    @Override
    public PageResult<Department> findAll(DepartmentQueryReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<Department> list = departmentMapper.selectByCondition(req.getName(), req.getParentId());
        PageInfo<Department> pageInfo = new PageInfo<>(list);
        return PageResult.success(list, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Department create(Department department) {
        int result = departmentMapper.insert(department);
        if (result <= 0) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_CREATE_FAILED);
        }
        return department;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Department update(Department department) {
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
    public void delete(Long id) {
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
