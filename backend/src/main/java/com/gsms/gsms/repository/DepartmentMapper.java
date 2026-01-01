package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {
    Department selectById(@Param("id") Long id);

    List<Department> selectAll();

    List<Department> selectByParentId(@Param("parentId") Long parentId);
    
    List<Department> selectByCondition(@Param("name") String name, @Param("parentId") Long parentId);

    int insert(Department department);

    int update(Department department);

    int deleteById(@Param("id") Long id);
}
