package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.OperationLog;
import com.gsms.gsms.model.enums.OperationModule;
import com.gsms.gsms.model.enums.OperationStatus;
import com.gsms.gsms.model.enums.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作日志Mapper接口
 */
@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {

    /**
     * 插入操作日志
     * @param log 操作日志实体
     * @return 影响行数
     */
    int insert(OperationLog log);

    /**
     * 根据条件查询操作日志
     * @param username 操作人用户名
     * @param module 操作模块
     * @param operationType 操作类型
     * @param status 操作状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 操作日志列表
     */
    List<OperationLog> findByCondition(
            @Param("username") String username,
            @Param("module") OperationModule module,
            @Param("operationType") OperationType operationType,
            @Param("status") OperationStatus status,
            @Param("startTime") String startTime,
            @Param("endTime") String endTime
    );

    /**
     * 根据ID查询操作日志
     * @param id 日志ID
     * @return 操作日志实体
     */
    OperationLog selectById(@Param("id") Long id);
}
