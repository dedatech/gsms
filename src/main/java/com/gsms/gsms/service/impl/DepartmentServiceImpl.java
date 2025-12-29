package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.domain.entity.Department;
import com.gsms.gsms.domain.enums.errorcode.DepartmentErrorCode;
import com.gsms.gsms.dto.department.DepartmentQueryReq;
import com.gsms.gsms.dto.department.DepartmentCreateReq;
import com.gsms.gsms.dto.department.DepartmentUpdateReq;
import com.gsms.gsms.dto.department.DepartmentConverter;
import com.gsms.gsms.dto.department.DepartmentInfoResp;
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
    public DepartmentInfoResp getById(Long id) {
        Department department = departmentMapper.selectById(id);
        if (department == null) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_NOT_FOUND);
        }
        return DepartmentInfoResp.from(department);
    }

    @Override
    public PageResult<DepartmentInfoResp> findAll(DepartmentQueryReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<Department> list = departmentMapper.selectByCondition(req.getName(), req.getParentId());
        PageInfo<Department> pageInfo = new PageInfo<>(list);
        List<DepartmentInfoResp> respList = DepartmentInfoResp.from(list);
        return PageResult.success(respList, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentInfoResp create(DepartmentCreateReq createReq) {
        // DTO转Entity
        Department department = DepartmentConverter.toEntity(createReq);
        int result = departmentMapper.insert(department);
        if (result <= 0) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_CREATE_FAILED);
        }
        return DepartmentInfoResp.from(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentInfoResp update(DepartmentUpdateReq updateReq) {
        // 检查部门是否存在
        getById(updateReq.getId());

        // DTO转Entity
        Department department = DepartmentConverter.toEntity(updateReq);
        int result = departmentMapper.update(department);
        if (result <= 0) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_UPDATE_FAILED);
        }

        Department updatedDepartment = departmentMapper.selectById(department.getId());
        return DepartmentInfoResp.from(updatedDepartment);
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
