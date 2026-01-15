package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.Project;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 项目Mapper接口
 */
@Mapper
public interface ProjectMapper extends BaseMapper<Project> {
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
     * 查询用户可访问的项目列表（基于项目成员表）
     * @param userId 用户ID
     * @return 项目列表
     */
    List<Project> selectAccessibleProjects(@Param("userId") Long userId);

    /**
     * 根据条件查询项目
     * @param project 查询条件（项目名称、状态等）
     * @return 项目列表
     */
    List<Project> selectByCondition(Project project);

    /**
     * 根据条件查询用户可访问的项目（基于项目成员表）
     * @param userId 用户ID
     * @param project 项目查询条件
     * @return 项目列表
     */
    List<Project> selectAccessibleProjectsByCondition(Project project);

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

    /**
     * 根据项目编码查询项目
     * @param code
     * @return
     */

    Project selectByCode(String code);

    /**
     * 根据编码前缀查询最大编号
     * 例如：查询 "CG" 开头的项目编码，返回最大数字部分（如 CG5 返回 5）
     *
     * @param prefix 编码前缀（如 "CG", "DX"）
     * @return 最大编号，如果没有匹配的记录返回 null
     */
    Integer selectMaxNumberByPrefix(@Param("prefix") String prefix);
}