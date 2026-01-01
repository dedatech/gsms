package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.Iteration;
import com.gsms.gsms.model.enums.IterationStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IterationMapper extends BaseMapper<Iteration> {
    Iteration selectById(@Param("id") Long id);

    List<Iteration> selectByProjectId(@Param("projectId") Long projectId);

    List<Iteration> selectByCondition(@Param("projectId") Long projectId, @Param("status") IterationStatus status);

    int insert(Iteration iteration);

    int update(Iteration iteration);

    int deleteById(@Param("id") Long id);
}