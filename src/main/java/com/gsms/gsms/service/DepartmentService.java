package com.gsms.gsms.service;

import com.gsms.gsms.dto.department.DepartmentQueryReq;
import com.gsms.gsms.dto.department.DepartmentCreateReq;
import com.gsms.gsms.dto.department.DepartmentUpdateReq;
import com.gsms.gsms.dto.department.DepartmentInfoResp;
import com.gsms.gsms.infra.common.PageResult;

/**
 * 部门服务接口
 */
public interface DepartmentService {
    /**
     * 根据ID查询部门
     * @param id 部门ID
     * @return 部门响应DTO
     */
    DepartmentInfoResp getById(Long id);

    /**
     * 根据条件分页查询部门
     * @param req 查询条件
     * @return 分页结果
     */
    PageResult<DepartmentInfoResp> findAll(DepartmentQueryReq req);

    /**
     * 创建部门
     * @param createReq 创建请求DTO
     * @return 创建成功的部门响应DTO
     */
    DepartmentInfoResp create(DepartmentCreateReq createReq);

    /**
     * 更新部门
     * @param updateReq 更新请求DTO
     * @return 更新后的部门响应DTO
     */
    DepartmentInfoResp update(DepartmentUpdateReq updateReq);

    /**
     * 删除部门
     * @param id 部门ID
     */
    void delete(Long id);
}