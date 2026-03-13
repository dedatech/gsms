package com.gsms.gsms.infra.aspect;

import com.gsms.gsms.infra.annotation.RequiresPermission;
import com.gsms.gsms.infra.exception.BusinessException;
import com.gsms.gsms.infra.exception.CommonErrorCode;
import com.gsms.gsms.infra.utils.UserContext;
import com.gsms.gsms.service.AuthService;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 权限校验切面
 * <p>
 * 拦截所有标记了 {@link RequiresPermission} 注解的方法，
 * 在方法执行前检查当前用户是否具备所需权限。
 * </p>
 *
 * <p>权限检查逻辑：</p>
 * <ul>
 *   <li>从 {@link UserContext} 获取当前登录用户ID</li>
 *   <li>通过 {@link AuthService#hasPermission(Long, String)} 检查权限</li>
 *   <li>如果权限不足，抛出 {@link CommonErrorCode#FORBIDDEN} 异常</li>
 * </ul>
 *
 * @author TeamMaster
 * @since 1.0.0
 */
@Aspect
@Component
public class PermissionAspect {

    private static final Logger logger = LoggerFactory.getLogger(PermissionAspect.class);

    private final AuthService authService;

    public PermissionAspect(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 在标记了 @RequiresPermission 注解的方法执行前进行权限校验
     *
     * @param joinPoint        连接点，包含被拦截方法的信息
     * @param requiresPermission 权限注解实例
     * @throws BusinessException 当用户未登录或权限不足时抛出
     */
    @Before("@annotation(requiresPermission)")
    public void checkPermission(JoinPoint joinPoint, RequiresPermission requiresPermission) {
        // 获取当前登录用户ID
        Long currentUserId = UserContext.getCurrentUserId();
        if (currentUserId == null) {
            logger.warn("用户未登录，尝试访问需要权限的方法: {}", joinPoint.getSignature());
            throw new BusinessException(CommonErrorCode.UNAUTHORIZED);
        }

        // 解析权限编码
        String[] permissions = requiresPermission.value().split(",");
        boolean requireAll = requiresPermission.requireAll();

        // 检查权限
        boolean hasPermission;
        if (requireAll) {
            // 需要拥有所有权限（AND 关系）
            hasPermission = Arrays.stream(permissions)
                .allMatch(perm -> authService.hasPermission(currentUserId, perm.trim()));

            if (!hasPermission) {
                logger.warn("用户{}缺少权限，访问被拒绝（需要所有权限）: {}",
                    currentUserId, Arrays.toString(permissions));
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        } else {
            // 拥有任一权限即可（OR 关系）
            hasPermission = Arrays.stream(permissions)
                .anyMatch(perm -> authService.hasPermission(currentUserId, perm.trim()));

            if (!hasPermission) {
                logger.warn("用户{}缺少权限，访问被拒绝（需要任一权限）: {}",
                    currentUserId, Arrays.toString(permissions));
                throw new BusinessException(CommonErrorCode.FORBIDDEN);
            }
        }

        // 权限校验通过
        logger.debug("用户{}权限校验通过: {}",
            currentUserId, Arrays.toString(permissions));
    }
}
