package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.Iteration;
import com.gsms.gsms.domain.enums.errorcode.IterationErrorCode;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.IterationMapper;
import com.gsms.gsms.service.IterationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class IterationServiceImpl implements IterationService {

    @Autowired
    private IterationMapper iterationMapper;

    @Override
    public Iteration getIterationById(Long id) {
        Iteration iteration = iterationMapper.selectById(id);
        if (iteration == null) {
            throw new BusinessException(IterationErrorCode.ITERATION_NOT_FOUND);
        }
        return iteration;
    }

    @Override
    public List<Iteration> getIterationsByProjectId(Long projectId) {
        return iterationMapper.selectByProjectId(projectId);
    }

    @Override
    public List<Iteration> getIterationsByCondition(Long projectId, Integer status) {
        return iterationMapper.selectByCondition(projectId, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Iteration createIteration(Iteration iteration) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        iteration.setCreateUserId(currentUserId);
        
        int result = iterationMapper.insert(iteration);
        if (result <= 0) {
            throw new BusinessException(IterationErrorCode.ITERATION_CREATE_FAILED);
        }
        
        return iteration;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Iteration updateIteration(Iteration iteration) {
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
    public void deleteIteration(Long id) {
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
