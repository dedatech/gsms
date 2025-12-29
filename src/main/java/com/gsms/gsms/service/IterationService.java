package com.gsms.gsms.service;

import com.gsms.gsms.dto.iteration.IterationQueryReq;
import com.gsms.gsms.dto.iteration.IterationCreateReq;
import com.gsms.gsms.dto.iteration.IterationUpdateReq;
import com.gsms.gsms.dto.iteration.IterationInfoResp;
import com.gsms.gsms.infra.common.PageResult;

/**
 * 迭代服务接口
 * 提供迭代的查询、创建、更新、删除等功能
 */
public interface IterationService {
    /**
     * 根据ID查询迭代
     *
     * @param id 迭代ID
     * @return 迭代信息DTO
     * @throws BusinessException 当迭代不存在时抛出
     */
    IterationInfoResp getById(Long id);

    /**
     * 根据条件分页查询迭代
     *
     * @param req 查询条件请求对象（包含项目ID、状态、分页参数等）
     * @return 分页结果
     */
    PageResult<IterationInfoResp> findAll(IterationQueryReq req);

    /**
     * 创建迭代
     *
     * @param createReq 迭代创建请求对象
     * @return 创建的迭代信息DTO
     * @throws BusinessException 当项目不存在或数据验证失败时抛出
     */
    IterationInfoResp create(IterationCreateReq createReq);

    /**
     * 更新迭代
     *
     * @param updateReq 迭代更新请求对象
     * @return 更新后的迭代信息DTO
     * @throws BusinessException 当迭代不存在或数据验证失败时抛出
     */
    IterationInfoResp update(IterationUpdateReq updateReq);

    /**
     * 删除迭代
     *
     * @param id 迭代ID
     * @throws BusinessException 当迭代不存在或有关联数据时抛出
     */
    void delete(Long id);
}