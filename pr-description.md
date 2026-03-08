# feat: 添加个人信息管理功能

## 📋 功能概述

实现用户个人信息管理功能，允许当前登录用户查看和修改个人基本信息、修改密码。通过右上角用户头像下拉菜单进入，提供友好的对话框交互界面。

## 🎯 主要变更

### 后端变更

#### 新增 DTO
- **UserProfileUpdateReq.java** - 用户个人信息更新请求 DTO
  - 支持更新昵称（2-20个字符）
  - 支持更新邮箱（格式验证）
  - 支持更新电话（11位手机号验证）

#### Controller 接口
- **GET /api/users/current** - 获取当前登录用户完整信息
  - 返回用户基本信息（用户名、昵称、邮箱、电话、部门、创建时间）
  - 自动填充部门名称

- **PUT /api/users/current** - 更新当前用户信息
  - 只允许修改昵称、邮箱、电话三个字段
  - 字段可选，只更新传入的非空字段
  - 记录审计日志（update_user_id、update_time）

- **PUT /api/users/current/password** - 修改当前用户密码
  - 验证旧密码（BCrypt 验证）
  - 新密码 BCrypt 加密存储
  - 记录审计日志

#### Service 方法
- **UserService.updateCurrentUserInfo()** - 更新用户信息
  - 智能判断是否有字段需要更新
  - 只更新有变化的字段
  - 完整的审计日志记录

- **UserService.changeCurrentPassword()** - 修改密码
  - 旧密码验证失败抛出异常
  - 使用专门的 Mapper 方法更新（避免枚举类型问题）
  - 清除缓存确保更新生效

#### Mapper 方法
- **UserMapper.updatePassword()** - 更新密码（新增）
  - 直接更新 password、update_user_id、update_time
  - 使用 @Options(flushCache = TRUE) 清除缓存
  - 避免触发 UserMapper.xml 动态 SQL

- **UserMapper.updateCurrentUserInfo()** - 更新用户信息（新增）
  - 动态 SQL 只更新传入的字段
  - 支持部分字段更新

### 前端变更

#### API 接口扩展
- **getCurrentUserInfo()** - 获取当前用户信息
- **updateCurrentUserInfo()** - 更新当前用户信息
- **changePassword()** - 修改密码

#### 组件
- **ProfileDialog.vue** - 个人信息对话框组件（新建）
  - **位置**：`frontend/src/components/user/ProfileDialog.vue`
  - **功能特性**：
    - 用户头像和基本信息展示
    - 基本信息编辑表单（昵称、邮箱、电话）
    - 密码修改表单（旧密码、新密码、确认密码）
    - 只读字段：用户名、部门、创建时间
    - 表单验证：
      - 昵称：2-20 个字符
      - 邮箱：标准邮箱格式
      - 电话：11 位手机号（1 开头）
      - 新密码：6-20 个字符
      - 确认密码：必须与新密码一致
    - 支持同时修改信息和密码
    - 修改成功后自动刷新用户信息

  - **样式特点**：
    - 使用 Element Plus 组件库
    - 遵循项目整体设计风格
    - 响应式布局
    - 友好的错误提示

#### 路由和页面
- **修改 Layout.vue** - 集成个人信息对话框
  - 在用户头像下拉菜单中添加 "个人信息" 入口
  - 点击后打开对话框
  - 修改成功后刷新用户信息显示

#### 状态管理
- **修改 auth.ts** - 添加 updateUserInfo() 方法
  - 支持更新用户信息到全局状态

## 🔒 安全特性

- ✅ **JWT Token 认证**：所有接口需要登录
- ✅ **权限控制**：用户只能修改自己的信息
- ✅ **旧密码验证**：BCrypt 加密验证旧密码
- ✅ **新密码加密**：BCrypt 加密存储新密码
- ✅ **参数校验**：前后端双重验证
  - 前端：Element Plus 表单验证
  - 后端：@Valid 注解验证
- ✅ **审计日志**：完整记录操作日志
- ✅ **缓存处理**：清除 MyBatis 缓存确保数据一致性

## 🧪 测试

### 测试用例覆盖（12 个测试）

#### 获取当前用户信息（2 个）
- ✅ testGetCurrentUserInfo_Success - 正常获取用户信息
- ✅ testGetCurrentUserInfo_Unauthorized - 未授权访问（401）

#### 更新当前用户信息（5 个）
- ✅ testUpdateCurrentUserInfo_Success - 更新所有可编辑字段
- ✅ testUpdateCurrentUserInfo_UpdateNicknameOnly - 只更新昵称
- ✅ testUpdateCurrentUserInfo_ValidationFailed_EmailFormat - 邮箱格式错误
- ✅ testUpdateCurrentUserInfo_ValidationFailed_PhoneFormat - 手机号格式错误
- ✅ testUpdateCurrentUserInfo_ValidationFailed_NicknameTooShort - 昵称太短

#### 修改密码（5 个）
- ✅ testChangeCurrentPassword_Success - 正常修改密码
- ✅ testChangeCurrentPassword_WrongOldPassword - 旧密码错误
- ✅ testChangeCurrentPassword_ValidationFailed_NewPasswordTooShort - 新密码太短
- ✅ testChangeCurrentPassword_ValidationFailed_MissingOldPassword - 缺少旧密码
- ✅ testChangeCurrentPassword_Unauthorized - 未授权访问（401）

### 测试类型
- **集成测试**：使用真实数据库和 Spring 上下文
- **控制器测试**：通过 MockMvc 模拟 HTTP 请求
- **测试数据库**：使用独立的 gsms_test 数据库（数据隔离）

## 📸 功能演示

### 使用流程

1. **查看个人信息**
   - 登录系统
   - 点击右上角用户头像
   - 选择 "个人信息"
   - 查看完整个人信息

2. **修改基本信息**
   - 在个人信息对话框中
   - 修改昵称、邮箱或电话
   - 点击"保存"按钮
   - 保存成功，信息实时更新

3. **修改密码**
   - 在"修改密码"区域
   - 输入旧密码（验证身份）
   - 输入新密码（6-20位）
   - 确认新密码
   - 点击"保存"按钮
   - 密码修改成功

### UI 特点
- 📱 响应式设计，适配不同屏幕
- 🎨 统一的 Element Plus 风格
- ✨ 友好的交互反馈
- 🔒 安全的密码输入（显示/隐藏切换）
- ⚠️ 实时表单验证

## 🔗 相关 Issue

 resolves 需求：用户需要能够查看和修改自己的个人信息

## 📌 Checklist

- [x] 功能开发完成
- [x] 前后端代码编写完成
- [x] 单元测试编写完成（12 个测试用例）
- [x] 代码符合项目规范（CLAUDE.md）
- [x] 前端组件遵循设计规范
- [x] 后端接口添加 Swagger 注解
- [x] 参数校验完整（前后端）
- [x] 安全性检查（认证、授权、加密）
- [x] 功能测试通过
- [x] 文档更新完整

## 📦 文件变更清单

### 后端文件（5 个文件）

#### 新建文件
```
backend/src/main/java/com/gsms/gsms/dto/user/
└── UserProfileUpdateReq.java          # 用户个人信息更新请求 DTO
```

#### 修改文件
```
backend/src/main/java/com/gsms/gsms/
├── controller/
│   └── UserController.java         # +3个接口方法
├── service/
│   ├── UserService.java            # +2个方法声明
│   └── impl/
│       └── UserServiceImpl.java    # +2个方法实现
├── repository/
│   └── UserMapper.java             # +2个方法声明
└── resources/mapper/
    └── UserMapper.xml               # +2个 SQL 语句
```

### 前端文件（4 个文件）

#### 新建文件
```
frontend/src/components/user/
└── ProfileDialog.vue               # 个人信息对话框组件
```

#### 修改文件
```
frontend/src/
├── api/
│   └── user.ts                     # +3个 API 方法
├── stores/
│   └── auth.ts                     # +updateUserInfo 方法
└── components/
    └── Layout.vue                   # +对话框集成
```

### 测试文件（1 个文件）

```
backend/src/test/java/com/gsms/gsms/controller/
└── UserControllerTest.java          # +12 个测试用例
```

## 💡 使用说明

### 1. 查看个人信息
```bash
# 入口：右上角用户头像 → 个人信息
# 显示：用户名、昵称、邮箱、电话、部门、创建时间
```

### 2. 修改基本信息
```bash
# 可修改字段：昵称、邮箱、电话
# 修改步骤：编辑 → 保存 → 实时更新
```

### 3. 修改密码
```bash
# 必填项：旧密码、新密码、确认密码
# 验证：旧密码正确、新密码格式正确、两次输入一致
# 修改后：需要重新登录（可选）
```

### 4. 字段限制
```bash
昵称：   2-20 个字符
邮箱：   标准邮箱格式
电话：   11 位手机号（1 开头）
新密码： 6-20 个字符
```

## 🐛 已知问题

- 无

## 📚 技术实现细节

### 后端技术栈
- Spring Boot 2.7.0
- MyBatis-Plus 3.5.3.1
- JWT 认证
- BCrypt 密码加密
- Spring Validation（参数校验）

### 前端技术栈
- Vue 3.4
- TypeScript 5.3
- Element Plus 2.5
- Pinia 2.1（状态管理）
- Axios 1.6（HTTP 客户端）

### 关键设计决策
1. **使用专用 Mapper 方法**：避免 MyBatis-Plus updateById 触发枚举类型处理问题
2. **清除缓存策略**：使用 @Options(flushCache = TRUE) 确保数据一致性
3. **对话框交互**：提供友好、集中的个人信息管理界面
4. **数据隔离**：测试使用独立的 gsms_test 数据库

## 📈 性能优化

- ✅ 使用专门的 Mapper 方法，避免触发复杂 SQL
- ✅ 清除 MyBatis 缓存，确保读取最新数据
- ✅ 前端按需加载用户信息
- ✅ 使用事务确保数据一致性

## 🔄 升级说明

### 数据库
- 无需数据库迁移（使用现有表结构）
- sys_user 表已包含所需字段

### 兼容性
- 向后兼容：不影响现有功能
- API 版本：无破坏性变更
- 前端：无缝集成，不影响现有页面

## 📞 联系方式

如有问题或建议，请：
1. 提交 Issue
2. 联系项目负责人
3. 参与代码审查

---

**Co-Authored-By: Claude Sonnet 4.6 <noreply@anthropic.com>**
**创建时间：2026-03-08**

**功能版本：1.0.0**
**相关文档：`CLAUDE.md`、`docs/development/frontend-architecture.md`**
