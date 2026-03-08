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
     * 插入用户（自定义方法）
     * @param user 用户实体
     * @return 影响行数
     */
    int insert(User user);  // 恢复原名，测试与 BaseMapper 的冲突

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

    /**
     * 查询所有用户
     * @return 用户列表
     */
    List<User> selectAll();

    /**
     * 更新用户密码
     * @param userId 用户ID
     * @param password 新密码
     * @param updateUserId 更新人ID
     * @return 影响行数
     */
    @org.apache.ibatis.annotations.Options(flushCache = org.apache.ibatis.annotations.Options.FlushCachePolicy.TRUE)
    int updatePassword(@Param("userId") Long userId,
                      @Param("password") String password,
                      @Param("updateUserId") Long updateUserId);

    /**
     * 更新当前用户信息（昵称、邮箱、电话）
     * @param userId 用户ID
     * @param nickname 昵称
     * @param email 邮箱
     * @param phone 电话
     * @param updateUserId 更新人ID
     * @return 影响行数
     */
    int updateCurrentUserInfo(@Param("userId") Long userId,
                             @Param("nickname") String nickname,
                             @Param("email") String email,
                             @Param("phone") String phone,
                             @Param("updateUserId") Long updateUserId);
}