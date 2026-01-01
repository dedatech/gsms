# 测试说明

## 测试结构

本项目采用了分层测试策略，包含以下测试类型：

1. **单元测试** - 针对各个独立组件的测试
2. **集成测试** - 针对组件间协作的测试
3. **接口测试** - 针对REST API的测试

## 测试目录结构

```
src/test/
├── java/com/gsms/gsms/
│   ├── controller/     # 控制器层测试
│   ├── service/        # 服务层测试
│   ├── mapper/         # 数据访问层测试
│   └── utils/          # 工具类测试
└── resources/          # 测试资源配置文件
```

## 测试类型说明

### 1. 控制器层测试 (Controller Tests)
- 位置：`src/test/java/com/gsms/gsms/controller/`
- 特点：使用`@WebMvcTest`注解，仅加载Web层相关配置
- 目的：验证HTTP请求处理、参数绑定、返回结果等

### 2. 服务层测试 (Service Tests)
- 位置：`src/test/java/com/gsms/gsms/service/`
- 特点：使用Mockito模拟依赖，隔离业务逻辑测试
- 目的：验证业务逻辑正确性

### 3. 数据访问层测试 (Mapper Tests)
- 位置：`src/test/java/com/gsms/gsms/mapper/`
- 特点：使用`@SpringBootTest`和`@Transactional`，需要真实数据库
- 目的：验证SQL映射和数据库操作正确性

### 4. 工具类测试 (Utils Tests)
- 位置：`src/test/java/com/gsms/gsms/utils/`
- 特点：独立的单元测试，无需Spring上下文
- 目的：验证工具类方法的正确性

## 运行测试

### 运行所有测试
```bash
mvn test
```

### 运行特定测试类
```bash
mvn test -Dtest=UserControllerTest
```

### 运行特定测试方法
```bash
mvn test -Dtest=UserControllerTest#testGetUserById_Success
```

## 数据库测试配置

Mapper层测试需要真实的数据库连接。请确保：

1. 数据库服务正在运行
2. 在`src/test/resources/application.yml`中配置正确的数据库连接
3. 测试数据表已创建

测试会在事务中执行并在结束后自动回滚，不会影响真实数据。

## 测试覆盖率

建议使用IDE的测试覆盖率工具或JaCoCo插件来检查测试覆盖率，确保关键业务逻辑得到充分测试。

## 编写新测试的注意事项

1. **命名规范**：测试方法名应清晰描述测试场景，格式为`test[MethodUnderTest]_[Scenario]`
2. **Given-When-Then结构**：使用注释清晰分隔测试的准备、执行和验证阶段
3. **断言明确**：每个测试应有明确的断言，验证预期结果
4. **独立性**：测试之间应保持独立，不互相依赖
5. **清理资源**：确保测试结束后清理临时资源