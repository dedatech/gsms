package com.gsms.gsms.dto.workhour;

import com.gsms.gsms.model.entity.WorkHour;

/**
 * 工时记录对象转换器
 */
public class WorkHourConverter {
    
    /**
     * 创建请求转工时记录实体
     */
    public static WorkHour toWorkHour(WorkHourCreateReq req) {
        if (req == null) {
            return null;
        }
        WorkHour workHour = new WorkHour();
        workHour.setProjectId(req.getProjectId());
        workHour.setTaskId(req.getTaskId());
        workHour.setWorkDate(req.getWorkDate());
        workHour.setHours(req.getHours());
        workHour.setContent(req.getContent());
        workHour.setStatus(req.getStatus());  // 直接设置枚举，MyBatis-Plus自动转换
        return workHour;
    }
    
    /**
     * 更新请求转工时记录实体
     */
    public static WorkHour toWorkHour(WorkHourUpdateReq req) {
        if (req == null) {
            return null;
        }
        WorkHour workHour = new WorkHour();
        workHour.setId(req.getId());
        workHour.setProjectId(req.getProjectId());
        workHour.setTaskId(req.getTaskId());
        workHour.setWorkDate(req.getWorkDate());
        workHour.setHours(req.getHours());
        workHour.setContent(req.getContent());
        workHour.setStatus(req.getStatus());  // 直接设置枚举，MyBatis-Plus自动转换
        return workHour;
    }
}
