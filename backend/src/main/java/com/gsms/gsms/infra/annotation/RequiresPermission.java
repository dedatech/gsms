package com.gsms.gsms.infra.annotation;

import java.lang.annotation.*;

/**
 * 权限校验注解
 * 用于标记需要特定权限才能访问的方法
 *
 * <p>使用示例：</p>
 * <pre>
 * // 单个权限
 * &#64;RequiresPermission("PROJECT_CREATE")
 * public Result&lt;Project&gt; createProject(...) { }
 *
 * // 多个权限（满足其一即可）
 * &#64;RequiresPermission("PROJECT_EDIT, PROJECT_DELETE")
 * public Result&lt;String&gt; updateProject(...) { }
 *
 * // 多个权限（必须全部满足）
 * &#64;RequiresPermission(value = "PROJECT_EDIT, PROJECT_DELETE", requireAll = true)
 * public Result&lt;String&gt; deleteProject(...) { }
 * </pre>
 *
 * @author TeamMaster
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequiresPermission {

    /**
     * 需要的权限编码
     * <p>支持多个权限，用逗号分隔，例如："PROJECT_EDIT, PROJECT_DELETE"</p>
     *
     * @return 权限编码字符串
     */
    String value();

    /**
     * 是否需要所有权限
     * <ul>
     *   <li>{@code true} - 需要拥有所有权限（AND 关系）</li>
     *   <li>{@code false} - 拥有任一权限即可（OR 关系），默认值</li>
     * </ul>
     *
     * @return 是否需要所有权限
     */
    boolean requireAll() default false;
}
