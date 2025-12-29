package com.gsms.gsms.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.gsms.gsms.domain.entity.Iteration;
import com.gsms.gsms.domain.enums.errorcode.IterationErrorCode;
import com.gsms.gsms.dto.iteration.IterationQueryReq;
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
    public Iteration getById(Long id) {
        Iteration iteration = iterationMapper.selectById(id);
        if (iteration == null) {
            throw new BusinessException(IterationErrorCode.ITERATION_NOT_FOUND);
        }
        return iteration;
    }

    @Override
    public PageResult<Iteration> findAll(IterationQueryReq req) {
        PageHelper.startPage(req.getPageNum(), req.getPageSize());
        List<Iteration> list = iterationMapper.selectByCondition(req.getProjectId(), req.getStatus());
        PageInfo<Iteration> pageInfo = new PageInfo<>(list);
        return PageResult.success(list, pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Iteration create(Iteration iteration) {
        Long currentUserId = UserContext.getCurrentUserId();
        iteration.setCreateUserId(currentUserId);

        int result = iterationMapper.insert(iteration);
        if (result <= 0) {
            throw new BusinessException(IterationErrorCode.ITERATION_CREATE_FAILED);
        }

        return iteration;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Iteration update(Iteration iteration) {
        Iteration existIteration = iterationMapper.selectById(iteration.getId());
        if (existIteration == null) {
            throw new BusinessException(IterationErrorCode.ITERATION_NOT_FOUND);
        }

        int result = iterationMapper.update(iteration);
        if (result <= 0) {
            throw new BusinessException(IterationErrorCode.ITERATION_UPDATE_FAILED);
        }

        return iterationMapper.selectById(iteration.getId());
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
