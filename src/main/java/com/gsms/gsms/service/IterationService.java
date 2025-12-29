package com.gsms.gsms.service;

import com.gsms.gsms.domain.entity.Iteration;
import com.gsms.gsms.dto.iteration.IterationQueryReq;
import com.gsms.gsms.infra.common.PageResult;

/**
 * 迭代服务接口
 */
public interface IterationService {
    /**
     * 根据ID查询迭代
     */
    Iteration getById(Long id);

    /**
     * 根据条件分页查询迭代
     */
    PageResult<Iteration> findAll(IterationQueryReq req);

    /**
     * 创建迭代
     */
    Iteration create(Iteration iteration);

    /**
     * 更新迭代
     */
    Iteration update(Iteration iteration);

    /**
     * 删除迭代
     */
    void delete(Long id);
}