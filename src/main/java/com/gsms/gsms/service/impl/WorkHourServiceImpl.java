package com.gsms.gsms.service.impl;

import com.gsms.gsms.entity.WorkHour;
import com.gsms.gsms.mapper.WorkHourMapper;
import com.gsms.gsms.service.WorkHourService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
        return workHourMapper.selectById(id);
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
    public boolean createWorkHour(WorkHour workHour) {
        return workHourMapper.insert(workHour) > 0;
    }

    @Override
    public boolean updateWorkHour(WorkHour workHour) {
        return workHourMapper.update(workHour) > 0;
    }

    @Override
    public boolean deleteWorkHour(Long id) {
        return workHourMapper.deleteById(id) > 0;
    }
}