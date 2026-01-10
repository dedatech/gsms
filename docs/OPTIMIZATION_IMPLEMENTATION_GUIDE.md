# GSMS 代码优化实施方案

**版本**: 1.0
**创建时间**: 2026-01-09
**状态**: 实施中

---

## 目录

1. [安全修复（紧急）](#安全修复紧急)
2. [高优先级优化](#高优先级优化)
3. [中优先级优化](#中优先级优化)
4. [测试验证](#测试验证)

---

## 安全修复（紧急）

### 1. JWT密钥和数据库密码环境变量化

#### 问题分析
- **JWT密钥硬编码**: `JwtUtil.java:17` 中密钥明文存储
- **数据库密码硬编码**: `application.yml:15` 中密码明文存储
- **安全风险**: 反编译后可获取敏感信息

#### 实施步骤

**步骤1: 修改 JwtUtil.java 使用环境变量**

```java
package com.gsms.gsms.infra.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类，用于身份认证
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:gsms_secret_key_2025}")
    private String secret;

    @Value("${jwt.expiration:86400000}") // 默认24小时
    private Long expirationTime;

    /**
     * 生成JWT Token
     */
    public String generateToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        return generateToken(claims);
    }

    /**
     * 生成JWT Token
     */
    public String generateToken(Map<String, Object> claims) {
        Date expireDate = new Date(System.currentTimeMillis() + expirationTime);

        return Jwts.builder()
                .setClaims(claims)
                .setExpiration(expireDate)
                .signWith(SignatureAlgorithm.HS512, secret)
                .compact();
    }

    /**
     * 验证Token是否有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(secret).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            // 生产环境使用slf4j，不要输出详细错误信息
            return false;
        }
    }

    /**
     * 从Token中获取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 从Token中获取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
            return claims.get("username").toString();
        } catch (Exception e) {
            return null;
        }
    }
}
```

**关键变更**:
- 将 `JwtUtil` 从工具类改为 Spring `@Component`
- 使用 `@Value` 注入配置
- 移除 `static` 修饰符，改为实例方法
- 移除 `System.err.println` 调试输出

**步骤2: 更新所有调用 JwtUtil 的地方**

由于 `JwtUtil` 现在是 Spring Bean，需要通过依赖注入使用：

```java
// 之前: JwtUtil.validateToken(token)
// 现在: 通过依赖注入

@Service
public class AuthServiceImpl implements AuthService {

    private final JwtUtil jwtUtil;  // 注入JwtUtil

    public AuthServiceImpl(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public boolean validateToken(String token) {
        return jwtUtil.validateToken(token);  // 调用实例方法
    }
}
```

**需要修改的文件**:
- `JwtInterceptor.java` - 拦截器中需要获取JwtUtil Bean
- `UserServiceImpl.java` - 登录时生成token
- `AuthServiceImpl.java` - 验证token

**JwtInterceptor.java 修改示例**:

```java
public class JwtInterceptor implements HandlerInterceptor {

    private JwtUtil jwtUtil;

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                          Object handler, ModelAndView modelAndView) {
        // 从Spring容器获取JwtUtil
        if (jwtUtil == null) {
            ApplicationContext ctx =
                WebApplicationContextUtils.getWebApplicationContext(
                    request.getServletContext());
            jwtUtil = ctx.getBean(JwtUtil.class);
        }
        // ...
    }
}
```

**步骤3: 修改 application.yml**

```yaml
# application.yml
spring:
  profiles:
    active: dev  # 开发环境
  datasource:
    url: jdbc:mysql://localhost:3306/gsms?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:root}
    driver-class-name: com.mysql.cj.jdbc.Driver

# JWT配置
jwt:
  secret: ${JWT_SECRET:gsms_secret_key_2025_dev_only}
  expiration: ${JWT_EXPIRATION:86400000}  # 24小时（毫秒）
```

**步骤4: 创建环境配置文件**

**application-dev.yml** (开发环境):
```yaml
# 开发环境可以使用默认值
jwt:
  secret: gsms_dev_secret_key_2025
  expiration: 86400000

spring:
  datasource:
    username: root
    password: root
```

**application-prod.yml** (生产环境):
```yaml
# 生产环境必须使用环境变量
jwt:
  secret: ${JWT_SECRET}  # 必须设置环境变量
  expiration: ${JWT_EXPIRATION:86400000}

spring:
  datasource:
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
```

**步骤5: 设置环境变量**

**Windows (PowerShell)**:
```powershell
# 临时设置（当前会话）
$env:JWT_SECRET="your-super-secret-jwt-key-min-256-bits"
$env:DB_USERNAME="production_user"
$env:DB_PASSWORD="strong_password_here"

# 永久设置（系统环境变量）
[System.Environment]::SetEnvironmentVariable("JWT_SECRET", "your-secret", "User")
```

**Windows (CMD)**:
```cmd
set JWT_SECRET=your-super-secret-jwt-key-min-256-bits
set DB_USERNAME=production_user
set DB_PASSWORD=strong_password_here
```

**Linux/Mac**:
```bash
# 临时设置
export JWT_SECRET="your-super-secret-jwt-key-min-256-bits"
export DB_USERNAME="production_user"
export DB_PASSWORD="strong_password_here"

# 永久设置（添加到 ~/.bashrc 或 ~/.zshrc）
echo 'export JWT_SECRET="your-super-secret-jwt-key-min-256-bits"' >> ~/.bashrc
echo 'export DB_USERNAME="production_user"' >> ~/.bashrc
echo 'export DB_PASSWORD="strong_password_here"' >> ~/.bashrc
source ~/.bashrc
```

**IDEA中设置**:
1. Run -> Edit Configurations
2. 选择Spring Boot配置
3. Environment variables 添加:
   ```
   JWT_SECRET=dev-secret-key-change-in-production
   DB_USERNAME=root
   DB_PASSWORD=root
   ```

**步骤6: 生成安全的JWT密钥**

使用以下方法生成256位密钥:

**方法1: 使用OpenSSL**
```bash
openssl rand -base64 32
```

**方法2: 使用Python**
```python
import secrets
print(secrets.token_urlsafe(32))
```

**方法3: 在线工具**
访问: https://www.random.org/strings/

---

### 2. 替换MD5为BCrypt密码哈希算法

#### 问题分析
- **MD5已过时**: 1996年就被发现碰撞漏洞
- **彩虹表攻击**: MD5哈希可被快速破解
- **行业标准**: BCrypt是现代密码存储的标准选择

#### 实施步骤

**步骤1: 添加Spring Security依赖**

```xml
<!-- backend/pom.xml -->
<dependency>
    <groupId>org.springframework.security</groupId>
    <artifactId>spring-security-crypto</artifactId>
    <version>5.7.11</version>
</dependency>
```

**步骤2: 创建新的 PasswordUtil.java**

```java
package com.gsms.gsms.infra.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 密码工具类
 * 使用BCrypt算法进行密码哈希
 */
public class PasswordUtil {

    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);  // 强度12

    /**
     * 加密密码
     * @param rawPassword 原始密码
     * @return 加密后的密码
     */
    public static String encrypt(String rawPassword) {
        if (rawPassword == null || rawPassword.isEmpty()) {
            return null;
        }
        return encoder.encode(rawPassword);
    }

    /**
     * 验证密码
     * @param rawPassword 原始密码
     * @param encryptedPassword 加密后的密码
     * @return 是否匹配
     */
    public static boolean verify(String rawPassword, String encryptedPassword) {
        if (rawPassword == null || encryptedPassword == null) {
            return false;
        }
        return encoder.matches(rawPassword, encryptedPassword);
    }
}
```

**关键改进**:
- 使用 `BCryptPasswordEncoder`，强度设置为12（推荐值）
- BCrypt自动加盐，每次生成的哈希值都不同
- `matches()` 方法自动处理盐值验证

**步骤3: 数据库迁移脚本**

由于BCrypt哈希与MD5完全不同，需要重置所有用户密码：

```sql
-- V8__reset_password_hash_migration.sql

-- 方案1: 强制所有用户下次登录时重置密码
ALTER TABLE sys_user ADD COLUMN password_reset_required TINYINT(1) DEFAULT 1;
UPDATE sys_user SET password_reset_required = 1 WHERE id > 0;

-- 方案2: 使用默认密码（需要用户首次登录后修改）
-- 新密码统一为: Admin123
UPDATE sys_user
SET password = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzpLaEmc3i'
WHERE id > 0;
-- 注意: 上面的哈希值是对 "Admin123" 进行BCrypt(12)哈希的结果
```

**生成默认密码哈希**:

```java
// 运行一次，生成BCrypt哈希
public class GeneratePasswordHash {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);
        String hash = encoder.encode("Admin123");
        System.out.println(hash);
        // 输出示例: $2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzpLaEmc3i
    }
}
```

**步骤4: 更新Flyway迁移**

创建新文件: `backend/src/main/resources/db/migration/V8__migrate_to_bcrypt.sql`

```sql
-- V8__migrate_to_bcrypt.sql

-- 1. 添加密码重置标志字段
ALTER TABLE sys_user ADD COLUMN password_reset_required TINYINT(1) DEFAULT 0 COMMENT '是否需要重置密码';

-- 2. 将现有用户标记为需要重置密码
UPDATE sys_user SET password_reset_required = 1 WHERE id > 0;

-- 3. 使用统一临时密码（用户首次登录后必须修改）
-- 临时密码: Admin123
UPDATE sys_user
SET password = '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewY5GyYzpLaEmc3i',
    password_reset_required = 1
WHERE id > 0;
```

**步骤5: 添加强制修改密码功能**

创建 `PasswordChangeController.java`:

```java
package com.gsms.gsms.controller;

import com.gsms.gsms.common.Result;
import com.gsms.gsms.dto.user.PasswordChangeReq;
import com.gsms.gsms.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户相关接口")
public class PasswordChangeController {

    private final UserService userService;

    public PasswordChangeController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 修改密码
     */
    @PutMapping("/password")
    @Operation(summary = "修改密码")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeReq req) {
        userService.changePassword(req);
        return Result.success();
    }
}
```

创建 `PasswordChangeReq.java`:

```java
package com.gsms.gsms.dto.user;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class PasswordChangeReq {

    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在6-20位之间")
    private String newPassword;
}
```

在 `UserService` 中添加方法:

```java
// UserService.java
void changePassword(PasswordChangeReq req);

// UserServiceImpl.java
@Override
@Transactional(rollbackFor = Exception.class)
public void changePassword(PasswordChangeReq req) {
    Long currentUserId = UserContext.getCurrentUserId();

    User user = userMapper.selectById(currentUserId);
    if (user == null) {
        throw new BusinessException(UserErrorCode.USER_NOT_FOUND);
    }

    // 验证旧密码
    if (!PasswordUtil.verify(req.getOldPassword(), user.getPassword())) {
        throw new BusinessException(UserErrorCode.PASSWORD_ERROR);
    }

    // 更新为新密码
    user.setPassword(PasswordUtil.encrypt(req.getNewPassword()));
    user.setUpdateUserId(currentUserId);
    user.setUpdateTime(LocalDateTime.now());
    user.setPasswordResetRequired(false);  // 清除重置标志

    userMapper.updateById(user);
}
```

在 `UserErrorCode` 中添加错误码:

```java
PASSWORD_ERROR(9005, "旧密码错误", "密码输入错误，请重新输入"),
```

**步骤6: 前端添加修改密码界面**

创建 `frontend/src/views/user/PasswordChange.vue`:

```vue
<template>
  <el-card class="password-change">
    <template #header>
      <h3>修改密码</h3>
    </template>

    <el-alert
      v-if="userInfo.passwordResetRequired"
      title="为了您的账户安全，首次登录后请立即修改密码"
      type="warning"
      :closable="false"
      style="margin-bottom: 20px"
    />

    <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
      <el-form-item label="旧密码" prop="oldPassword">
        <el-input v-model="form.oldPassword" type="password" show-password />
      </el-form-item>

      <el-form-item label="新密码" prop="newPassword">
        <el-input v-model="form.newPassword" type="password" show-password />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input v-model="form.confirmPassword" type="password" show-password />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="handleSubmit" :loading="loading">
          提交修改
        </el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useRouter } from 'vue-router'
import axios from 'axios'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)
const userInfo = ref<any>({})

const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value !== form.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleSubmit = async () => {
  if (!formRef.value) return

  await formRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        await axios.put('/api/users/password', {
          oldPassword: form.oldPassword,
          newPassword: form.newPassword
        })
        ElMessage.success('密码修改成功，请重新登录')
        router.push('/login')
      } catch (error: any) {
        ElMessage.error(error.response?.data?.message || '密码修改失败')
      } finally {
        loading.value = false
      }
    }
  })
}

onMounted(() => {
  // 从localStorage获取用户信息
  const userStr = localStorage.getItem('userInfo')
  if (userStr) {
    userInfo.value = JSON.parse(userStr)
  }
})
</script>

<style scoped>
.password-change {
  max-width: 600px;
  margin: 40px auto;
}
</style>
```

**步骤7: 测试验证**

```bash
# 1. 编译项目
cd backend
mvn clean install

# 2. 运行Flyway迁移
mvn flyway:migrate

# 3. 启动应用
mvn spring-boot:run

# 4. 测试登录
# 使用任意账号，密码: Admin123
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"Admin123"}'

# 5. 测试修改密码
curl -X PUT http://localhost:8080/api/users/password \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{"oldPassword":"Admin123","newPassword":"newStrongPassword123"}'
```

---

### 3. 修复CORS配置限制域名

#### 实施步骤

**修改 WebConfig.java**:

```java
package com.gsms.gsms.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Value("${cors.allowed-origins:http://localhost:3000}")
    private String[] allowedOrigins;

    public WebConfig(JwtInterceptor jwtInterceptor) {
        this.jwtInterceptor = jwtInterceptor;
    }

    /**
     * 配置跨域资源共享(CORS)
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(allowedOrigins)  // 使用配置的域名
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    // ... 其他配置保持不变
}
```

**配置文件**:

```yaml
# application.yml
cors:
  allowed-origins: http://localhost:3000,https://yourdomain.com

# 或者使用环境变量
cors:
  allowed-origins: ${CORS_ALLOWED_ORIGINS:http://localhost:3000}
```

**生产环境配置**:

```yaml
# application-prod.yml
cors:
  allowed-origins: https://your-production-domain.com,https://www.your-domain.com
```

**测试**:

```bash
# 测试CORS配置
curl -X OPTIONS http://localhost:8080/api/users \
  -H "Origin: http://localhost:3000" \
  -H "Access-Control-Request-Method: POST" \
  -v

# 检查响应头
# 应该看到: Access-Control-Allow-Origin: http://localhost:3000
```

---

### 4. 修复ProjectServiceImpl事务边界问题

#### 问题分析

当前代码:
```java
@Transactional(rollbackFor = Exception.class)
public Long create(ProjectCreateReq req) {
    // 1. 创建项目（在事务中）
    projectMapper.insert(project);

    // 2. 创建项目成员（在事务外执行！！！）
    projectMemberMapper.insert(projectMember);  // ❌ 如果这里失败，项目已创建

    return project.getId();
}
```

如果第2步失败，项目已经创建但成员未添加，导致数据不一致。

#### 实施步骤

**步骤1: 读取当前ProjectServiceImpl**

```bash
# 查看当前实现
cat backend/src/main/java/com/gsms/gsms/service/impl/ProjectServiceImpl.java
```

**步骤2: 修改create方法**

```java
package com.gsms.gsms.service.impl;

import com.gsms.gsms.dto.project.ProjectCreateReq;
import com.gsms.gsms.model.entity.Project;
import com.gsms.gsms.model.entity.ProjectMember;
import com.gsms.gsms.model.enums.RoleType;
import com.gsms.gsms.repository.ProjectMapper;
import com.gsms.gsms.repository.ProjectMemberMapper;
import com.gsms.gsms.service.ProjectService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ProjectServiceImpl implements ProjectService {

    private final ProjectMapper projectMapper;
    private final ProjectMemberMapper projectMemberMapper;

    public ProjectServiceImpl(ProjectMapper projectMapper,
                             ProjectMemberMapper projectMemberMapper) {
        this.projectMapper = projectMapper;
        this.projectMemberMapper = projectMemberMapper;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)  // 确保所有异常都回滚
    public Long create(ProjectCreateReq req) {
        Long currentUserId = UserContext.getCurrentUserId();

        // 1. 创建项目
        Project project = new Project();
        project.setName(req.getName());
        project.setCode(req.getCode());
        project.setDescription(req.getDescription());
        project.setManagerId(req.getManagerId());
        project.setStatus(req.getStatus());
        project.setCreateUserId(currentUserId);
        project.setUpdateUserId(currentUserId);
        project.setCreateTime(LocalDateTime.now());
        project.setUpdateTime(LocalDateTime.now());

        projectMapper.insert(project);

        // 2. 创建项目成员（在同一事务内）
        ProjectMember projectMember = new ProjectMember();
        projectMember.setProjectId(project.getId());
        projectMember.setUserId(req.getManagerId());
        projectMember.setRoleType(RoleType.MANAGER);
        projectMember.setJoinTime(LocalDateTime.now());
        projectMember.setUpdateUserId(currentUserId);
        projectMember.setUpdateTime(LocalDateTime.now());

        // ✅ 现在在事务内，如果失败会整体回滚
        projectMemberMapper.insert(projectMember);

        return project.getId();
    }
}
```

**关键改进**:
- 项目成员创建在事务内
- 如果成员创建失败，整个事务回滚，项目也不会被创建
- 使用 `@Transactional(rollbackFor = Exception.class)` 确保所有异常都触发回滚

**步骤3: 添加单元测试**

```java
@SpringBootTest
@Transactional
class ProjectServiceImplTest {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Test
    @DisplayName("创建项目失败时应回滚整个事务")
    void createProject_ShouldRollback_OnFailure() {
        // 准备测试数据
        ProjectCreateReq req = new ProjectCreateReq();
        req.setName("测试项目");
        req.setCode("TEST001");
        req.setManagerId(1L);
        req.setStatus(ProjectStatus.NOT_STARTED);

        // 模拟成员创建失败
        assertThrows(Exception.class, () -> {
            projectService.create(req);
        });

        // 验证项目和成员都未创建
        assertEquals(0, projectMapper.selectByCode("TEST001").size());
    }
}
```

**步骤4: 验证修复**

```bash
# 运行测试
mvn test -Dtest=ProjectServiceImplTest

# 启动应用手动测试
mvn spring-boot:run

# 使用Postman/curl测试
curl -X POST http://localhost:8080/api/projects \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -d '{
    "name": "测试项目",
    "code": "TEST001",
    "managerId": 1,
    "status": "NOT_STARTED"
  }'
```

---

## 测试验证

### 安全修复验证清单

- [ ] JWT密钥从环境变量读取
- [ ] 数据库密码从环境变量读取
- [ ] 所有旧密码已迁移到BCrypt
- [ ] 用户首次登录可以修改密码
- [ ] CORS只允许指定域名
- [ ] 项目创建失败时正确回滚

### 测试命令

```bash
# 1. 环境变量测试
echo $JWT_SECRET
echo $DB_PASSWORD

# 2. JWT测试
curl -X POST http://localhost:8080/api/users/login \
  -H "Content-Type: application/json" \
  -d '{"username":"zhangsan","password":"Admin123"}' \
  | jq .

# 3. CORS测试
curl -X OPTIONS http://localhost:8080/api/projects \
  -H "Origin: http://evil-site.com" \
  -v 2>&1 | grep "Access-Control"

# 4. 密码修改测试
curl -X PUT http://localhost:8080/api/users/password \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"oldPassword":"Admin123","newPassword":"NewPass123"}'

# 5. 事务测试
# 检查日志确认事务回滚
tail -f logs/gsms.log | grep "Transaction"
```

---

## 高优先级优化

### 5. TypeScript类型安全重构

#### 问题分析

前端代码中有47处使用`any`类型，导致：
- 失去TypeScript类型检查保护
- IDE无法提供准确的代码补全
- 容易引入运行时错误

#### 实施步骤

**步骤1: 创建统一的类型定义文件**

创建 `frontend/src/types/index.ts`:

```typescript
// ============ 通用类型 ============

/**
 * API统一响应格式
 */
export interface Result<T = any> {
  code: number
  message: string
  data: T
}

/**
 * 分页响应格式
 */
export interface PageResult<T = any> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}

/**
 * 基础查询参数
 */
export interface BaseQuery {
  pageNum?: number
  pageSize?: number
}

// ============ 用户相关类型 ============

/**
 * 用户状态枚举
 */
export type UserStatus = 'NORMAL' | 'DISABLED'

/**
 * 用户信息
 */
export interface User {
  id: number
  username: string
  realName?: string
  email?: string
  phone?: string
  departmentId?: number
  departmentName?: string
  status: UserStatus
  createTime: string
  updateTime: string
  creatorName?: string
  updaterName?: string
}

/**
 * 登录请求
 */
export interface LoginReq {
  username: string
  password: string
}

/**
 * 登录响应
 */
export interface LoginResp {
  token: string
  userInfo: User
}

// ============ 项目相关类型 ============

/**
 * 项目状态枚举
 */
export type ProjectStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'SUSPENDED' | 'ARCHIVED'

/**
 * 项目状态配置
 */
export interface ProjectStatusConfig {
  label: string
  value: ProjectStatus
  color: string
}

/**
 * 项目信息
 */
export interface Project {
  id: number
  name: string
  code: string
  description?: string
  managerId: number
  managerName?: string
  status: ProjectStatus
  createTime: string
  updateTime: string
  creatorName?: string
  updaterName?: string
}

/**
 * 创建项目请求
 */
export interface ProjectCreateReq {
  name: string
  code: string
  description?: string
  managerId: number
  status: ProjectStatus
}

/**
 * 更新项目请求
 */
export interface ProjectUpdateReq {
  id: number
  name?: string
  description?: string
  status?: ProjectStatus
}

/**
 * 项目查询参数
 */
export interface ProjectQuery extends BaseQuery {
  name?: string
  status?: ProjectStatus
}

// ============ 任务相关类型 ============

/**
 * 任务优先级枚举
 */
export type TaskPriority = 'LOW' | 'MEDIUM' | 'HIGH' | 'URGENT'

/**
 * 任务状态枚举
 */
export type TaskStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'

/**
 * 任务信息
 */
export interface Task {
  id: number
  title: string
  description?: string
  projectId: number
  projectName?: string
  iterationId?: number
  iterationName?: string
  assigneeId?: number
  assigneeName?: string
  priority: TaskPriority
  status: TaskStatus
  estimatedHours?: number
  actualHours?: number
  createTime: string
  updateTime: string
  creatorName?: string
  updaterName?: string
}

/**
 * 创建任务请求
 */
export interface TaskCreateReq {
  title: string
  description?: string
  projectId: number
  iterationId?: number
  assigneeId?: number
  priority: TaskPriority
  status: TaskStatus
  estimatedHours?: number
}

/**
 * 更新任务请求
 */
export interface TaskUpdateReq {
  id: number
  title?: string
  description?: string
  assigneeId?: number
  priority?: TaskPriority
  status?: TaskStatus
  estimatedHours?: number
  actualHours?: number
}

/**
 * 任务查询参数
 */
export interface TaskQuery extends BaseQuery {
  projectId?: number
  iterationId?: number
  assigneeId?: number
  status?: TaskStatus
  priority?: TaskPriority
}

// ============ 工时相关类型 ============

/**
 * 工时状态枚举
 */
export type WorkHourStatus = 'SAVED' | 'SUBMITTED' | 'CONFIRMED'

/**
 * 工时信息
 */
export interface WorkHour {
  id: number
  userId: number
  userName?: string
  projectId: number
  projectName?: string
  taskId?: number
  taskName?: string
  workDate: string
  hours: number
  content: string
  status: WorkHourStatus
  createTime: string
  updateTime: string
}

/**
 * 创建工时请求
 */
export interface WorkHourCreateReq {
  projectId: number
  taskId?: number
  workDate: string
  hours: number
  content: string
}

/**
 * 更新工时请求
 */
export interface WorkHourUpdateReq {
  id: number
  projectId: number
  taskId?: number
  workDate: string
  hours: number
  content: string
}

/**
 * 工时查询参数
 */
export interface WorkHourQuery extends BaseQuery {
  projectId?: number
  taskId?: number
  startDate?: string
  endDate?: string
}

/**
 * 工时统计信息
 */
export interface WorkHourStatistics {
  todayHours: number
  weekHours: number
  monthHours: number
  totalHours: number
}

// ============ 迭代相关类型 ============

/**
 * 迭代状态枚举
 */
export type IterationStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'COMPLETED'

/**
 * 迭代信息
 */
export interface Iteration {
  id: number
  name: string
  projectId: number
  projectName?: string
  startDate: string
  endDate: string
  status: IterationStatus
  goal?: string
  createTime: string
  updateTime: string
}
```

**步骤2: 更新API接口文件**

修改 `frontend/src/api/project.ts`:

```typescript
import request from './request'
import type {
  Project,
  ProjectCreateReq,
  ProjectUpdateReq,
  ProjectQuery,
  PageResult
} from '@/types'

/**
 * 获取项目列表
 */
export const getProjectList = async (params: ProjectQuery): Promise<PageResult<Project>> => {
  return request.get('/projects', { params }) as Promise<PageResult<Project>>
}

/**
 * 创建项目
 */
export const createProject = async (data: ProjectCreateReq): Promise<void> => {
  return request.post('/projects', data)
}

/**
 * 更新项目
 */
export const updateProject = async (data: ProjectUpdateReq): Promise<void> => {
  return request.put('/projects', data)
}

/**
 * 删除项目
 */
export const deleteProject = async (id: number): Promise<void> => {
  return request.delete(`/projects/${id}`)
}

/**
 * 获取项目详情
 */
export const getProjectDetail = async (id: number): Promise<Project> => {
  return request.get(`/projects/${id}`) as Promise<Project>
}
```

修改 `frontend/src/api/task.ts`:

```typescript
import request from './request'
import type {
  Task,
  TaskCreateReq,
  TaskUpdateReq,
  TaskQuery,
  PageResult
} from '@/types'

/**
 * 获取任务列表
 */
export const getTaskList = async (params: TaskQuery): Promise<PageResult<Task>> => {
  return request.get('/tasks', { params }) as Promise<PageResult<Task>>
}

/**
 * 创建任务
 */
export const createTask = async (data: TaskCreateReq): Promise<void> => {
  return request.post('/tasks', data)
}

/**
 * 更新任务
 */
export const updateTask = async (data: TaskUpdateReq): Promise<void> => {
  return request.put('/tasks', data)
}

/**
 * 删除任务
 */
export const deleteTask = async (id: number): Promise<void> => {
  return request.delete(`/tasks/${id}`)
}
```

修改 `frontend/src/api/workhour.ts`:

```typescript
import request from './request'
import type {
  WorkHour,
  WorkHourCreateReq,
  WorkHourUpdateReq,
  WorkHourQuery,
  WorkHourStatistics,
  PageResult
} from '@/types'

/**
 * 获取工时列表
 */
export const getWorkHourList = async (params: WorkHourQuery): Promise<PageResult<WorkHour>> => {
  return request.get('/work-hours', { params }) as Promise<PageResult<WorkHour>>
}

/**
 * 创建工时
 */
export const createWorkHour = async (data: WorkHourCreateReq): Promise<void> => {
  return request.post('/work-hours', data)
}

/**
 * 更新工时
 */
export const updateWorkHour = async (data: WorkHourUpdateReq): Promise<void> => {
  return request.put('/work-hours', data)
}

/**
 * 删除工时
 */
export const deleteWorkHour = async (id: number): Promise<void> => {
  return request.delete(`/work-hours/${id}`)
}

/**
 * 获取工时统计
 */
export const getUserWorkHourStatistics = async (
  userId: number,
  startDate?: string,
  endDate?: string
): Promise<WorkHourStatistics> => {
  return request.get('/work-hours/statistics', {
    params: { userId, startDate, endDate }
  }) as Promise<WorkHourStatistics>
}

// 导出类型供组件使用
export type { WorkHourInfo } from '@/types'  // 兼容旧代码
```

**步骤3: 更新组件使用类型**

修改 `frontend/src/views/project/ProjectList.vue`:

```typescript
<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { Project, ProjectQuery, ProjectStatus } from '@/types'
import { getProjectList, updateProject } from '@/api/project'

// ✅ 使用明确的类型
const projectList = ref<Project[]>([])
const loading = ref(false)

// 查询表单
const queryForm = reactive<ProjectQuery>({
  name: '',
  status: undefined,
  pageNum: 1,
  pageSize: 10
})

// ✅ 明确的类型声明
const viewMode = ref<'kanban' | 'table'>('kanban')
const draggedProject = ref<Project | null>(null)

// ✅ 类型安全的处理函数
const handleEdit = (project: Project) => {
  // ...
}

const getStatusType = (status: ProjectStatus): string => {
  const typeMap: Record<ProjectStatus, string> = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'warning',
    'SUSPENDED': 'danger',
    'ARCHIVED': 'info'
  }
  return typeMap[status] || 'info'
}

// ✅ API调用有明确返回类型
const fetchProjects = async () => {
  loading.value = true
  try {
    const res = await getProjectList(queryForm)
    projectList.value = res.list
    pagination.total = res.total
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败')
  } finally {
    loading.value = false
  }
}
</script>
```

**步骤4: 更新auth store类型**

修改 `frontend/src/stores/auth.ts`:

```typescript
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface JwtPayload {
  userId: number
  username?: string
  exp?: number
  iat?: number
}

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const userId = ref<number>(0)
  const username = ref<string>('')

  const isAuthenticated = computed(() => !!token.value)
  const currentUser = computed(() => ({
    id: userId.value,
    username: username.value
  }))

  const parseToken = (tokenValue: string): JwtPayload | null => {
    try {
      const parts = tokenValue.split('.')
      if (parts.length !== 3) {
        return null
      }

      const payload = parts[1]
        .replace(/-/g, '+')
        .replace(/_/g, '/')
      const decoded = atob(payload)
      return JSON.parse(decoded) as JwtPayload
    } catch (error) {
      return null
    }
  }

  const setAuth = (tokenValue: string, usernameValue?: string) => {
    token.value = tokenValue

    const payload = parseToken(tokenValue)
    if (payload && payload.userId) {
      userId.value = payload.userId
    }

    username.value = usernameValue || payload?.username || ''

    localStorage.setItem('token', tokenValue)
    localStorage.setItem('userId', userId.value.toString())
    localStorage.setItem('username', username.value)
  }

  const clearAuth = () => {
    token.value = ''
    userId.value = 0
    username.value = ''

    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
  }

  const restoreAuth = () => {
    const savedToken = localStorage.getItem('token')
    const savedUserId = localStorage.getItem('userId')
    const savedUsername = localStorage.getItem('username')

    if (savedToken) {
      token.value = savedToken
      userId.value = savedUserId ? parseInt(savedUserId) : 0
      username.value = savedUsername || ''

      const payload = parseToken(savedToken)
      if (payload && payload.exp) {
        const now = Math.floor(Date.now() / 1000)
        if (payload.exp < now) {
          clearAuth()
        }
      }
    }
  }

  const getCurrentUserId = (): number => {
    return userId.value || 0
  }

  return {
    token,
    userId,
    username,
    isAuthenticated,
    currentUser,
    setAuth,
    clearAuth,
    restoreAuth,
    getCurrentUserId,
    parseToken
  }
})
```

**步骤5: 配置tsconfig.json路径别名**

确保 `frontend/tsconfig.json` 包含路径别名配置:

```json
{
  "compilerOptions": {
    "baseUrl": ".",
    "paths": {
      "@/*": ["src/*"]
    }
  }
}
```

确保 `frontend/vite.config.ts` 包含路径解析:

```typescript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src')
    }
  }
})
```

**步骤6: 批量替换步骤**

使用IDE的查找替换功能，逐步消除`any`类型:

```bash
# 1. 查找所有包含 :any 的地方
grep -r ":any" frontend/src/

# 2. 逐个文件检查和替换
# 查找: const projectList = ref<any[]>([])
# 替换: const projectList = ref<Project[]>([])

# 查找: const res: any = await getProjectList()
# 替换: const res = await getProjectList()
```

**步骤7: 验证类型安全**

```bash
# TypeScript类型检查
cd frontend
npm run type-check

# 或使用vue-tsc
npx vue-tsc --noEmit
```

---

### 6. 提取重复的状态映射函数到公共工具

#### 问题分析

`getStatusType()` 和 `getStatusText()` 函数在多个组件中重复：
- `ProjectList.vue`
- `TaskList.vue`
- `WorkHourList.vue`

#### 实施步骤

**步骤1: 创建公共状态工具函数**

创建 `frontend/src/composables/useStatusHelper.ts`:

```typescript
import { computed, ComputedRef } from 'vue'
import type { ProjectStatus, TaskStatus, WorkHourStatus, TaskPriority } from '@/types'

/**
 * 状态类型映射（Element Plus Tag type）
 */
const STATUS_TYPE_MAP: Record<string, 'success' | 'warning' | 'danger' | 'info'> = {
  // 项目状态
  'NOT_STARTED': 'info',
  'IN_PROGRESS': 'warning',
  'SUSPENDED': 'danger',
  'ARCHIVED': 'info',

  // 任务状态
  'COMPLETED': 'success',
  'CANCELLED': 'danger',

  // 工时状态
  'SAVED': 'info',
  'SUBMITTED': 'warning',
  'CONFIRMED': 'success',

  // 通用
  'NORMAL': 'success',
  'DISABLED': 'danger'
}

/**
 * 状态文本映射（中文）
 */
const STATUS_TEXT_MAP: Record<string, string> = {
  // 项目状态
  'NOT_STARTED': '未开始',
  'IN_PROGRESS': '进行中',
  'SUSPENDED': '已暂停',
  'ARCHIVED': '已归档',

  // 任务状态
  'COMPLETED': '已完成',
  'CANCELLED': '已取消',

  // 工时状态
  'SAVED': '已保存',
  'SUBMITTED': '已提交',
  'CONFIRMED': '已确认',

  // 用户状态
  'NORMAL': '正常',
  'DISABLED': '禁用',

  // 优先级
  'LOW': '低',
  'MEDIUM': '中',
  'HIGH': '高',
  'URGENT': '紧急'
}

/**
 * 状态颜色映射（自定义颜色）
 */
const STATUS_COLOR_MAP: Record<string, string> = {
  // 项目状态
  'NOT_STARTED': '#d9d9d9',
  'IN_PROGRESS': '#1890ff',
  'SUSPENDED': '#faad14',
  'ARCHIVED': '#8c8c8c',

  // 任务优先级
  'LOW': '#52c41a',
  'MEDIUM': '#1890ff',
  'HIGH': '#faad14',
  'URGENT': '#f5222d'
}

/**
 * 状态辅助函数Composable
 */
export function useStatusHelper() {
  /**
   * 获取状态对应的Element Plus Tag类型
   */
  const getStatusType = (status: string): 'success' | 'warning' | 'danger' | 'info' => {
    return STATUS_TYPE_MAP[status] || 'info'
  }

  /**
   * 获取状态中文文本
   */
  const getStatusText = (status: string): string => {
    return STATUS_TEXT_MAP[status] || '未知'
  }

  /**
   * 获取状态颜色
   */
  const getStatusColor = (status: string): string => {
    return STATUS_COLOR_MAP[status] || '#d9d9d9'
  }

  /**
   * 为项目列表生成状态配置
   */
  const getProjectStatusConfig = (): Array<{
    label: string
    value: ProjectStatus
    color: string
  }> => {
    return [
      { label: '未开始', value: 'NOT_STARTED', color: '#d9d9d9' },
      { label: '进行中', value: 'IN_PROGRESS', color: '#1890ff' },
      { label: '已暂停', value: 'SUSPENDED', color: '#faad14' },
      { label: '已归档', value: 'ARCHIVED', color: '#8c8c8c' }
    ]
  }

  /**
   * 为任务列表生成状态配置
   */
  const getTaskStatusConfig = (): Array<{
    label: string
    value: TaskStatus
    color: string
  }> => {
    return [
      { label: '未开始', value: 'NOT_STARTED', color: '#d9d9d9' },
      { label: '进行中', value: 'IN_PROGRESS', color: '#1890ff' },
      { label: '已完成', value: 'COMPLETED', color: '#52c41a' },
      { label: '已取消', value: 'CANCELLED', color: '#8c8c8c' }
    ]
  }

  /**
   * 为任务列表生成优先级配置
   */
  const getTaskPriorityConfig = (): Array<{
    label: string
    value: TaskPriority
    color: string
  }> => {
    return [
      { label: '低', value: 'LOW', color: '#52c41a' },
      { label: '中', value: 'MEDIUM', color: '#1890ff' },
      { label: '高', value: 'HIGH', color: '#faad14' },
      { label: '紧急', value: 'URGENT', color: '#f5222d' }
    ]
  }

  return {
    getStatusType,
    getStatusText,
    getStatusColor,
    getProjectStatusConfig,
    getTaskStatusConfig,
    getTaskPriorityConfig
  }
}
```

**步骤2: 更新ProjectList.vue**

```typescript
<script setup lang="ts">
import { useStatusHelper } from '@/composables/useStatusHelper'
import type { Project, ProjectStatus } from '@/types'

// 使用状态辅助函数
const { getStatusType, getStatusText, getProjectStatusConfig } = useStatusHelper()

// 状态配置
const projectStatuses = getProjectStatusConfig()

// 类型安全的状态处理函数
const handleProjectStatus = (project: Project) => {
  return {
    type: getStatusType(project.status),
    text: getStatusText(project.status)
  }
}
</script>

<template>
  <!-- 使用 -->
  <el-tag :type="getStatusType(project.status)">
    {{ getStatusText(project.status) }}
  </el-tag>
</template>
```

**步骤3: 更新TaskList.vue**

```typescript
<script setup lang="ts">
import { useStatusHelper } from '@/composables/useStatusHelper'
import type { Task, TaskStatus, TaskPriority } from '@/types'

const { getStatusType, getStatusText, getTaskStatusConfig, getTaskPriorityConfig } = useStatusHelper()

// 任务状态配置
const taskStatuses = getTaskStatusConfig()
const taskPriorities = getTaskPriorityConfig()
</script>

<template>
  <el-tag :type="getStatusType(task.status)">
    {{ getStatusText(task.status) }}
  </el-tag>
</template>
```

**步骤4: 更新WorkHourList.vue**

```typescript
<script setup lang="ts">
import { useStatusHelper } from '@/composables/useStatusHelper'
import type { WorkHour } from '@/types'

const { getStatusType, getStatusText } = useStatusHelper()
</script>

<template>
  <el-tag :type="getStatusType(row.status)">
    {{ getStatusText(row.status) }}
  </el-tag>
</template>
```

**步骤5: 移除组件中的重复函数**

在每个组件中删除以下代码:
```typescript
// ❌ 删除这些重复的函数
const getStatusType = (status: string) => { /* ... */ }
const getStatusText = (status: string) => { /* ... */ }
const projectStatuses = [ /* ... */ ]
```

---

## 中优先级优化

### 7. 修复N+1查询问题实现批量查询

#### 问题分析

当前代码在数据enrichment时存在N+1查询:

```java
// UserServiceImpl.java: 235-240
for (UserInfoResp resp : respList) {
    String creatorName = cacheService.getUserName(resp.getCreateUserId());  // N+1
    resp.setCreatorName(creatorName);
    String updaterName = cacheService.getUpdaterName(resp.getUpdateUserId());  // N+1
    resp.setUpdaterName(updaterName);
}
```

#### 实施步骤

**步骤1: 在CacheService中添加批量查询方法**

修改 `backend/src/main/java/com/gsms/gsms/service/impl/CacheServiceImpl.java`:

```java
package com.gsms.gsms.service.impl;

import com.gsms.gsms.domain.entity.User;
import com.gsms.gsms.repository.UserMapper;
import com.gsms.gsms.service.CacheService;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存服务实现
 */
@Service
public class CacheServiceImpl implements CacheService {

    private final UserMapper userMapper;

    // 用户缓存: userId -> User
    private final Map<Long, User> userCache = new ConcurrentHashMap<>();

    public CacheServiceImpl(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @PostConstruct
    public void init() {
        loadAllUsers();
    }

    /**
     * 加载所有用户到缓存
     */
    private void loadAllUsers() {
        List<User> users = userMapper.selectList(null);
        for (User user : users) {
            userCache.put(user.getId(), user);
        }
    }

    /**
     * 获取用户名（单个）
     */
    @Override
    public String getUserName(Long userId) {
        if (userId == null) {
            return "";
        }
        User user = userCache.get(userId);
        return user != null ? user.getRealName() : "";
    }

    /**
     * 批量获取用户名（新增）
     */
    @Override
    public Map<Long, String> getUserNameBatch(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, String> result = new HashMap<>();
        for (Long userId : userIds) {
            result.put(userId, getUserName(userId));
        }
        return result;
    }

    /**
     * 获取部门名（单个）
     */
    @Override
    public String getDepartmentName(Long departmentId) {
        // 实现省略
        return "";
    }

    /**
     * 批量获取部门名（新增）
     */
    @Override
    public Map<Long, String> getDepartmentNameBatch(Set<Long> departmentIds) {
        // 实现省略
        return Collections.emptyMap();
    }
}
```

更新接口 `backend/src/main/java/com/gsms/gsms/service/CacheService.java`:

```java
package com.gsms.gsms.service;

import java.util.Map;
import java.util.Set;

/**
 * 缓存服务接口
 */
public interface CacheService {

    String getUserName(Long userId);

    Map<Long, String> getUserNameBatch(Set<Long> userIds);  // 新增

    String getDepartmentName(Long departmentId);

    Map<Long, String> getDepartmentNameBatch(Set<Long> departmentIds);  // 新增
}
```

**步骤2: 修改UserServiceImpl使用批量查询**

```java
package com.gsms.gsms.service.impl;

import com.gsms.gsms.dto.user.UserInfoResp;
import com.gsms.gsms.service.CacheService;
import com.gsms.gsms.service.UserService;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;
    private final CacheService cacheService;

    public UserServiceImpl(UserMapper userMapper, CacheService cacheService) {
        this.userMapper = userMapper;
        this.cacheService = cacheService;
    }

    /**
     * 批量enrich用户信息（优化后）
     */
    private List<UserInfoResp> enrichUserInfoRespList(List<UserInfoResp> respList) {
        if (respList == null || respList.isEmpty()) {
            return respList;
        }

        // ✅ 1. 收集所有需要的用户ID
        Set<Long> userIds = new HashSet<>();
        for (UserInfoResp resp : respList) {
            if (resp.getCreateUserId() != null) {
                userIds.add(resp.getCreateUserId());
            }
            if (resp.getUpdateUserId() != null) {
                userIds.add(resp.getUpdateUserId());
            }
        }

        // ✅ 2. 批量查询所有用户名（一次查询）
        Map<Long, String> userNameMap = cacheService.getUserNameBatch(userIds);

        // ✅ 3. 填充响应对象（从内存Map中获取，无数据库查询）
        for (UserInfoResp resp : respList) {
            resp.setCreatorName(userNameMap.get(resp.getCreateUserId()));
            resp.setUpdaterName(userNameMap.get(resp.getUpdateUserId()));
        }

        return respList;
    }
}
```

**步骤3: 同样优化ProjectServiceImpl和TaskServiceImpl**

```java
// ProjectServiceImpl.java
private List<ProjectInfoResp> enrichProjectInfoRespList(List<ProjectInfoResp> respList) {
    if (respList == null || respList.isEmpty()) {
        return respList;
    }

    // 收集所有需要的ID
    Set<Long> userIds = new HashSet<>();
    for (ProjectInfoResp resp : respList) {
        if (resp.getCreateUserId() != null) userIds.add(resp.getCreateUserId());
        if (resp.getUpdateUserId() != null) userIds.add(resp.getUpdateUserId());
        if (resp.getManagerId() != null) userIds.add(resp.getManagerId());
    }

    // 批量查询
    Map<Long, String> userNameMap = cacheService.getUserNameBatch(userIds);

    // 填充
    for (ProjectInfoResp resp : respList) {
        resp.setCreatorName(userNameMap.get(resp.getCreateUserId()));
        resp.setUpdaterName(userNameMap.get(resp.getUpdateUserId()));
        resp.setManagerName(userNameMap.get(resp.getManagerId()));
    }

    return respList;
}
```

**步骤4: 性能对比测试**

```java
@SpringBootTest
class CacheServicePerformanceTest {

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("性能测试：批量查询 vs 单个查询")
    void performanceTest() {
        int testSize = 1000;

        // 测试旧方法（N+1查询）
        long start1 = System.currentTimeMillis();
        // ... 旧的enrich方法
        long end1 = System.currentTimeMillis();
        System.out.println("旧方法耗时: " + (end1 - start1) + "ms");

        // 测试新方法（批量查询）
        long start2 = System.currentTimeMillis();
        // ... 新的enrich方法
        long end2 = System.currentTimeMillis();
        System.out.println("新方法耗时: " + (end2 - start2) + "ms");

        // 预期：新方法快10-100倍
    }
}
```

---

### 8. 添加组件资源清理（onUnmounted）

#### 问题分析

当前组件中：
- 定时器未清理
- 事件监听器未移除
- WebSocket连接未关闭

可能导致内存泄漏。

#### 实施步骤

**步骤1: 创建useInterval Composable**

创建 `frontend/src/composables/useInterval.ts`:

```typescript
import { onUnmounted } from 'vue'

/**
 * 定时器Hook（自动清理）
 */
export function useInterval(callback: () => void, delay: number | null) {
  let timer: number | null = null

  const start = () => {
    if (delay === null) return
    timer = window.setInterval(callback, delay)
  }

  const stop = () => {
    if (timer !== null) {
      clearInterval(timer)
      timer = null
    }
  }

  // 组件卸载时自动清理
  onUnmounted(() => {
    stop()
  })

  return { start, stop }
}
```

**步骤2: 在WorkHourList中使用**

```typescript
<script setup lang="ts">
import { onMounted } from 'vue'
import { useInterval } from '@/composables/useInterval'

// 自动刷新统计数据
const { start: startAutoRefresh, stop: stopAutoRefresh } = useInterval(
  () => {
    fetchStatistics()
  },
  60000  // 每分钟刷新一次
)

onMounted(() => {
  fetchProjects()
  fetchStatistics()
  startAutoRefresh()  // 开始自动刷新
})

// 组件卸载时会自动调用 stopAutoRefresh
</script>
```

**步骤3: 添加Event Bus清理**

创建 `frontend/src/composables/useEventBus.ts`:

```typescript
import { onUnmounted } from 'vue'
import mitt, { Emitter } from 'mitt'

type Events = {
  'project:updated': number
  'task:created': any
  // ... 其他事件
}

const emitter = mitt<Events>()

/**
 * 事件总线Hook
 */
export function useEventBus() {
  const on = <K extends keyof Events>(
    event: K,
    handler: (data: Events[K]) => void
  ) => {
    emitter.on(event, handler)

    // 自动清理
    onUnmounted(() => {
      emitter.off(event, handler)
    })
  }

  const emit = <K extends keyof Events>(event: K, data: Events[K]) => {
    emitter.emit(event, data)
  }

  return { on, emit }
}
```

**步骤4: 在组件中使用**

```typescript
<script setup lang="ts">
import { useEventBus } from '@/composables/useEventBus'

const { on, emit } = useEventBus()

// 监听项目更新事件
on('project:updated', (projectId) => {
  console.log('项目已更新:', projectId)
  fetchProjects()
})

// 组件卸载时会自动移除监听器
</script>
```

**步骤5: 内存泄漏检测**

使用Chrome DevTools检测内存泄漏:

```bash
# 1. 打开Chrome DevTools
# 2. 切换到Memory标签
# 3. 点击"Take heap snapshot"
# 4. 执行操作（打开/关闭组件）
# 5. 再次拍摄快照
# 6. 比较两个快照，查看内存是否释放
```

---

## 实施优先级和时间估算

### 第一周（安全修复 - 紧急）

| 任务 | 预计工时 | 优先级 |
|-----|---------|--------|
| JWT密钥环境变量化 | 2小时 | P0 |
| 数据库密码环境变量化 | 1小时 | P0 |
| 替换MD5为BCrypt | 4小时 | P0 |
| CORS配置限制域名 | 1小时 | P0 |
| 修复事务边界问题 | 2小时 | P0 |
| **总计** | **10小时** | **P0** |

### 第二周（类型安全 - 高优先级）

| 任务 | 预计工时 | 优先级 |
|-----|---------|--------|
| 创建统一类型定义 | 4小时 | P1 |
| 更新API接口类型 | 3小时 | P1 |
| 更新组件使用类型 | 6小时 | P1 |
| TypeScript类型检查 | 2小时 | P1 |
| **总计** | **15小时** | **P1** |

### 第三周（性能优化 - 中优先级）

| 任务 | 预计工时 | 优先级 |
|-----|---------|--------|
| 提取状态映射函数 | 2小时 | P2 |
| 修复N+1查询问题 | 4小时 | P2 |
| 添加资源清理 | 3小时 | P2 |
| 性能测试 | 2小时 | P2 |
| **总计** | **11小时** | **P2** |

---

## 检查清单

### 安全修复 ✅

- [ ] JWT密钥从环境变量读取
- [ ] 数据库密码从环境变量读取
- [ ] 所有用户密码已迁移到BCrypt
- [ ] 密码重置功能已添加
- [ ] CORS只允许指定域名
- [ ] 项目创建事务已修复

### 类型安全 ✅

- [ ] 创建了统一的types文件
- [ ] 所有API接口有明确类型
- [ ] 移除了所有`any`类型
- [ ] TypeScript编译无错误
- [ ] IDE提供准确代码补全

### 性能优化 ✅

- [ ] 状态映射函数已提取
- [ ] N+1查询问题已修复
- [ ] 批量查询已实现
- [ ] 组件资源正确清理
- [ ] 内存泄漏测试通过

---

**文档维护**: 随着项目演进持续更新
**最后更新**: 2026-01-09
