package com.gsms.gsms.repository;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.gsms.gsms.model.entity.User;
import com.gsms.gsms.model.enums.UserStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    /**
     * 根据ID查询用户
     * @param id 用户ID
     * @return 用户实体
     */
    User selectById(@Param("id") Long id);

    /**
     * 批量根据ID列表查询用户
     * @param ids 用户ID列表
     * @return 用户列表
     */
    List<User> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 根据用户名查询用户
     * @param username 用户名
     * @return 用户实体
     */
    User selectByUsername(@Param("username") String username);

    /**
     * 根据条件查询用户
     * @param username 用户名（模糊匹配）
     * @param status 用户状态
     * @return 用户列表
     */
    List<User> findAll(User user);

    /**
     * 插入用户
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(User user);

    /**
     * 更新用户
     * @param user 用户实体
     * @return 影响行数
     */
    int update(User user);

    /**
     * 根据ID删除用户
     * @param id 用户ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据部门ID查询用户列表
     * @param departmentId 部门ID
     * @return 用户列表
     */
    List<User> selectByDepartmentId(@Param("departmentId") Long departmentId);
}