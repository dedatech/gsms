package com.gsms.gsms.service;

import com.gsms.gsms.dto.iteration.IterationQueryReq;
import com.gsms.gsms.dto.iteration.IterationCreateReq;
import com.gsms.gsms.dto.iteration.IterationUpdateReq;
import com.gsms.gsms.dto.iteration.IterationInfoResp;
import com.gsms.gsms.infra.common.PageResult;

/**
 * 迭代服务接口
 */
public interface IterationService {
    /**
     * 根据ID查询迭代
     */
    IterationInfoResp getById(Long id);

    /**
     * 根据条件分页查询迭代
     */
    PageResult<IterationInfoResp> findAll(IterationQueryReq req);

    /**
     * 创建迭代
     */
    IterationInfoResp create(IterationCreateReq createReq);

    /**
     * 更新迭代
     */
    IterationInfoResp update(IterationUpdateReq updateReq);

    /**
     * 删除迭代
     */
    void delete(Long id);
}