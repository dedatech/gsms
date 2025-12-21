package com.gsms.gsms.mapper;

import com.gsms.gsms.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目Mapper接口
 */
@Mapper
public interface ProjectMapper {
    /**
     * 根据ID查询项目
     * @param id 项目ID
     * @return 项目实体
     */
    Project selectById(@Param("id") Long id);

    /**
     * 查询所有项目
     * @return 项目列表
     */
    List<Project> selectAll();

    /**
     * 根据条件查询项目
     * @param name 项目名称（模糊匹配）
     * @param status 项目状态
     * @return 项目列表
     */
    List<Project> selectByCondition(@Param("name") String name, @Param("status") Integer status);

    /**
     * 插入项目
     * @param project 项目实体
     * @return 影响行数
     */
    int insert(Project project);

    /**
     * 更新项目
     * @param project 项目实体
     * @return 影响行数
     */
    int update(Project project);

    /**
     * 根据ID删除项目
     * @param id 项目ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);
}