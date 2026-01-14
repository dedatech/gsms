package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.model.entity.Department;
import com.gsms.gsms.model.enums.errorcode.DepartmentErrorCode;
import com.gsms.gsms.dto.department.DepartmentQueryReq;
import com.gsms.gsms.dto.department.DepartmentCreateReq;
import com.gsms.gsms.dto.department.DepartmentUpdateReq;
import com.gsms.gsms.dto.department.DepartmentConverter;
import com.gsms.gsms.dto.department.DepartmentInfoResp;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.DepartmentMapper;
import com.gsms.gsms.service.CacheService;
import com.gsms.gsms.service.DepartmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 部门服务实现类
 */
@Service
public class DepartmentServiceImpl implements DepartmentService {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentServiceImpl.class);

    private final DepartmentMapper departmentMapper;
    private final CacheService cacheService;

    public DepartmentServiceImpl(DepartmentMapper departmentMapper, CacheService cacheService) {
        this.departmentMapper = departmentMapper;
        this.cacheService = cacheService;
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

        // 设置审计字段
        Long currentUserId = UserContext.getCurrentUserId();
        department.setCreateUserId(currentUserId != null ? currentUserId : 1L);
        department.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = departmentMapper.insert(department);
        if (result <= 0) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_CREATE_FAILED);
        }

        // 更新缓存：将新部门添加到缓存中
        cacheService.putDepartment(department);
        logger.info("部门创建成功并已更新缓存: id={}, name={}", department.getId(), department.getName());

        return DepartmentInfoResp.from(department);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentInfoResp update(DepartmentUpdateReq updateReq) {
        // 检查部门是否存在
        getById(updateReq.getId());

        // DTO转Entity
        Department department = DepartmentConverter.toEntity(updateReq);

        // 设置审计字段
        Long currentUserId = UserContext.getCurrentUserId();
        department.setUpdateUserId(currentUserId != null ? currentUserId : 1L);

        int result = departmentMapper.update(department);
        if (result <= 0) {
            throw new BusinessException(DepartmentErrorCode.DEPARTMENT_UPDATE_FAILED);
        }

        // 更新缓存：重新从数据库查询并更新缓存
        Department updatedDepartment = departmentMapper.selectById(department.getId());
        cacheService.putDepartment(updatedDepartment);
        logger.info("部门更新成功并已更新缓存: id={}, name={}", updatedDepartment.getId(), updatedDepartment.getName());

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

        // 更新缓存：从缓存中移除已删除的部门
        cacheService.removeDepartment(id);
        logger.info("部门删除成功并已更新缓存: id={}, name={}", id, existDepartment.getName());
    }
}
