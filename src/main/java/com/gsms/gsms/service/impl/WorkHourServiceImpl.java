package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.WorkHour;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.domain.enums.errorcode.WorkHourErrorCode;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.repository.WorkHourMapper;
import com.gsms.gsms.service.WorkHourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 工时记录服务实现类
 */
@Service
public class WorkHourServiceImpl implements WorkHourService {

    @Autowired
    private WorkHourMapper workHourMapper;

    @Override
    public WorkHour getWorkHourById(Long id) {
        WorkHour workHour = workHourMapper.selectById(id);
        if (workHour == null) {
            throw new BusinessException(WorkHourErrorCode.WORKHOUR_NOT_FOUND);
        }
        return workHour;
    }

    @Override
    public List<WorkHour> getWorkHoursByUserId(Long userId) {
        return workHourMapper.selectByUserId(userId);
    }

    @Override
    public List<WorkHour> getWorkHoursByProjectId(Long projectId) {
        return workHourMapper.selectByProjectId(projectId);
    }

    @Override
    public List<WorkHour> getWorkHoursByCondition(Long userId, Long projectId, Date startDate, Date endDate) {
        return workHourMapper.selectByCondition(userId, projectId, startDate, endDate);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkHour createWorkHour(WorkHour workHour) {
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        workHour.setUserId(currentUserId); // 工时记录的 userId 就是当前登录用户
        
        int result = workHourMapper.insert(workHour);
        if (result <= 0) {
            throw new BusinessException(WorkHourErrorCode.WORKHOUR_CREATE_FAILED);
        }
        
        return workHour;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WorkHour updateWorkHour(WorkHour workHour) {
        // 检查工时记录是否存在
        WorkHour existWorkHour = workHourMapper.selectById(workHour.getId());
        if (existWorkHour == null) {
            throw new BusinessException(WorkHourErrorCode.WORKHOUR_NOT_FOUND);
        }
        
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }
        workHour.setUpdateUserId(currentUserId);
        
        int result = workHourMapper.update(workHour);
        if (result <= 0) {
            throw new BusinessException(WorkHourErrorCode.WORKHOUR_UPDATE_FAILED);
        }
        
        return workHourMapper.selectById(workHour.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteWorkHour(Long id) {
        // 检查工时记录是否存在
        WorkHour existWorkHour = workHourMapper.selectById(id);
        if (existWorkHour == null) {
            throw new BusinessException(WorkHourErrorCode.WORKHOUR_NOT_FOUND);
        }
        
        int result = workHourMapper.deleteById(id);
        if (result <= 0) {
            throw new BusinessException(WorkHourErrorCode.WORKHOUR_DELETE_FAILED);
        }
    }
}