package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Department;
import com.gsms.gsms.dto.department.DepartmentQueryReq;
import com.gsms.gsms.infra.common.PageResult;

/**
 * 部门服务接口
 */
public interface DepartmentService {
    /**
     * 根据ID查询部门
     * @param id 部门ID
     * @return 部门实体
     */
    Department getById(Long id);

    /**
     * 根据条件分页查询部门
     * @param req 查询条件
     * @return 分页结果
     */
    PageResult<Department> findAll(DepartmentQueryReq req);

    /**
     * 创建部门
     * @param department 部门实体
     * @return 创建成功的部门实体
     */
    Department create(Department department);

    /**
     * 更新部门
     * @param department 部门实体
     * @return 更新后的部门实体
     */
    Department update(Department department);

    /**
     * 删除部门
     * @param id 部门ID
     */
    void delete(Long id);
}