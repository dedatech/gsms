package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.domain.entity.Iteration;
import com.gsms.gsms.domain.enums.errorcode.IterationErrorCode;
import com.gsms.gsms.dto.iteration.IterationQueryReq;
import com.gsms.gsms.dto.iteration.IterationCreateReq;
import com.gsms.gsms.dto.iteration.IterationUpdateReq;
import com.gsms.gsms.dto.iteration.IterationConverter;
import com.gsms.gsms.dto.iteration.IterationInfoResp;
import com.gsms.gsms.infra.common.PageResult;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.IterationMapper;
import com.gsms.gsms.service.IterationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 迭代服务实现类
 */
@Service
public class IterationServiceImpl implements IterationService {

    private final IterationMapper iterationMapper;

    public IterationServiceImpl(IterationMapper iterationMapper) {
        this.iterationMapper = iterationMapper;
    }

    @Override
    public IterationInfoResp getById(Long id) {
        Iteration iteration = iterationMapper.selectById(id);
        if (iteration == null) {
            throw new BusinessException(IterationErrorCode.ITERATION_NOT_FOUND);
        }
        return IterationInfoResp.from(iteration);
    }

    @Override
    public PageResult<IterationInfoResp> findAll(IterationQueryReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<Iteration> list = iterationMapper.selectByCondition(req.getProjectId(), req.getStatus());
        PageInfo<Iteration> pageInfo = new PageInfo<>(list);
        List<IterationInfoResp> respList = IterationInfoResp.from(list);
        return PageResult.success(respList, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IterationInfoResp create(IterationCreateReq createReq) {
        // DTO转Entity
        Iteration iteration = IterationConverter.toEntity(createReq);
        Long currentUserId = UserContext.getCurrentUserId();
        iteration.setCreateUserId(currentUserId);

        int result = iterationMapper.insert(iteration);
        if (result <= 0) {
            throw new BusinessException(IterationErrorCode.ITERATION_CREATE_FAILED);
        }

        return IterationInfoResp.from(iteration);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IterationInfoResp update(IterationUpdateReq updateReq) {
        // 检查迭代是否存在
        getById(updateReq.getId());

        // DTO转Entity
        Iteration iteration = IterationConverter.toEntity(updateReq);

        int result = iterationMapper.update(iteration);
        if (result <= 0) {
            throw new BusinessException(IterationErrorCode.ITERATION_UPDATE_FAILED);
        }

        Iteration updatedIteration = iterationMapper.selectById(iteration.getId());
        return IterationInfoResp.from(updatedIteration);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Iteration existIteration = iterationMapper.selectById(id);
        if (existIteration == null) {
            throw new BusinessException(IterationErrorCode.ITERATION_NOT_FOUND);
        }

        int result = iterationMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(IterationErrorCode.ITERATION_DELETE_FAILED);
        }
    }
}
