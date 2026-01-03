# 枚举类型JSON序列化统一改造总结

**日期**: 2026-01-03
**提交**: `52f7b3c`

## 背景与目标

### 需求
统一所有API层状态字段的输出格式，从数字码改为字符串常量（枚举name），提升接口可读性和自描述性。

### 目标数据流
```
JSON请求("NOT_STARTED") → DTO(ProjectStatus.NOT_STARTED) → Entity(ProjectStatus.NOT_STARTED) → DB(tinyint:1)
```

## 实施方案

### 1. 枚举类改造

所有枚举类统一使用以下模式：

```java
public enum ProjectStatus {
    NOT_STARTED(1, "未开始"),
    IN_PROGRESS(2, "进行中"),
    SUSPENDED(3, "已挂起"),
    ARCHIVED(4, "已归档");

    @EnumValue  // MyBatis-Plus 标记存储到数据库的值
    private final Integer code;
    private final String desc;

    @JsonValue  // Jackson 序列化为JSON时输出的值（枚举的 name）
    @Override
    public String toString() {
        return this.name();
    }

    public Integer getCode() {
        return code;
    }
}
```

**关键点**：
- `@EnumValue` 标记 `code` 字段，MyBatis-Plus 使用此值存入数据库
- `@JsonValue` + `toString()` 返回 `name()`，Jackson 序列化为JSON时输出字符串

### 2. DTO调整

部分DTO的 status 字段从 `Integer` 改为枚举类型：

- `UserQueryReq.status`: `Integer` → `UserStatus`
- `ProjectCreateReq.status`: 保持 `ProjectStatus`
- `ProjectUpdateReq.status`: 保持 `ProjectStatus`

### 3. MyBatis配置

#### 3.1 启用全局枚举配置

在 `application.yml` 和 `application-test.yml` 中启用：

```yaml
mybatis-plus:
  type-enums-package: com.gsms.gsms.model.enums
  configuration:
    default-enum-type-handler: com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler
```

#### 3.2 Mapper XML显式typeHandler

在所有涉及枚举字段的SQL中显式指定 typeHandler：

```xml
<!-- ResultMap -->
<result column="status" property="status"
        typeHandler="com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler"/>

<!-- INSERT -->
INSERT INTO gsms_project(..., status, ...)
VALUES(..., #{status, typeHandler=com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler}, ...)

<!-- UPDATE -->
<if test="status != null">status = #{status, typeHandler=com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler},</if>

<!-- WHERE条件 -->
<if test="status != null">
    AND status = #{status, typeHandler=com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler}
</if>
```

**为什么必须显式指定？**
- 全局配置 `default-enum-type-handler` 对 MyBatis-Plus 的 BaseMapper 方法有效
- 自定义XML SQL不会自动应用全局配置，需要显式声明

### 4. 查询方法调整

#### 问题
`ProjectMapper.selectAccessibleProjectsByCondition` 原本使用多参数：

```java
List<Project> selectAccessibleProjectsByCondition(
    @Param("userId") Long userId,
    @Param("project") Project project
);
```

在WHERE条件中使用 `#{project.status}` 导致 typeHandler 失败。

#### 错误信息
```
Could not find @EnumValue in Class: java.lang.Object
```

#### 解决方案
改为单参数方法，通过对象属性设置过滤条件：

```java
// Mapper接口
List<Project> selectAccessibleProjectsByCondition(Project project);

// XML
<where>
    AND p.is_deleted = 0 AND pm.user_id = #{managerId}
    <if test="status != null">
        AND p.status = #{status, typeHandler=...}
    </if>
</where>

// Service调用
query.setManagerId(currentUserId);
projects = projectMapper.selectAccessibleProjectsByCondition(query);
```

## 修改范围

### 枚举类（6个）
- ✅ `UserStatus.java`
- ✅ `ProjectStatus.java`
- ✅ `IterationStatus.java`
- ✅ `TaskStatus.java`
- ✅ `TaskPriority.java`
- ✅ `WorkHourStatus.java`

### Mapper接口（1个）
- ✅ `ProjectMapper.java` - `selectAccessibleProjectsByCondition` 方法签名

### Mapper XML（4个）
- ✅ `UserMapper.xml`
- ✅ `ProjectMapper.xml`
- ✅ `IterationMapper.xml`
- ✅ `TaskMapper.xml`

### Service实现（2个）
- ✅ `UserServiceImpl.java`
- ✅ `ProjectServiceImpl.java`

### 配置文件（3个）
- ✅ `application.yml`
- ✅ `application-test.yml`
- ✅ `application-prod.yml`

### 测试用例（4个Controller）
- ✅ `UserControllerTest.java` - 10/10 通过
- ✅ `ProjectControllerTest.java` - 10/10 通过
- ✅ `IterationControllerTest.java`
- ✅ `TaskControllerTest.java`

## 已知技术问题

### 问题描述

在 MyBatis XML 的 WHERE 条件中，使用**对象属性路径**（如 `#{project.status}`）时，MyBatis 无法正确推断 `MybatisEnumTypeHandler` 的泛型类型，导致类型转换失败。

### 错误信息

```
java.lang.IllegalArgumentException: Could not find @EnumValue in Class: java.lang.Object.
at com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler.lambda$new$0(MybatisEnumTypeHandler.java:65)
```

### 根本原因

`MybatisEnumTypeHandler<T>` 是一个泛型抽象类：
- MyBatis 在处理 `#{property}`（直接属性）时，能够通过 Java 反射正确推断泛型类型
- MyBatis 在处理 `#{object.property}`（对象属性路径）时，类型推断机制失效，默认使用 `Object.class`
- `Object.class` 没有 `@EnumValue` 注解，导致 typeHandler 初始化失败

### 临时解决方案

1. **调整方法参数**：将多参数改为单对象参数
2. **使用直接属性**：WHERE条件中使用 `#{property}` 而非 `#{object.property}`
3. **Service层设置过滤**：通过 setter 方法设置过滤条件（如 `setManagerId()`）

### 适用场景对比

| 场景 | 是否支持 | 说明 |
|-----|---------|------|
| `#{status}` | ✅ 支持 | 直接属性，类型推断正常 |
| `#{project.status}` | ❌ 不支持 | 对象属性路径，类型推断失败 |
| INSERT/UPDATE | ✅ 支持 | 使用 `#{status}` |
| WHERE条件 | ✅ 部分支持 | 需使用 `#{status}` 而非 `#{object.status}` |

### 后续待办

- [ ] 深入研究 MyBatis 类型推断机制
- [ ] 考虑自定义具体的 TypeHandler 类（避免泛型推断问题）
- [ ] 调研 MyBatis-Plus 官方是否有推荐方案
- [ ] 评估其他ORM框架（如 JPA）的枚举处理方式

## 效果验证

### API响应示例

**改造前**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "GSMS项目",
    "status": 1
  }
}
```

**改造后**：
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "GSMS项目",
    "status": "NOT_STARTED"
  }
}
```

### 测试结果

- ✅ UserController: 10/10 测试通过
- ✅ ProjectController: 10/10 测试通过
- ✅ IterationController: 全部通过
- ✅ TaskController: 全部通过

### 数据库验证

```sql
-- 数据库中仍存储为整数（tinyint）
SELECT id, name, status FROM gsms_project;
-- 输出: 1, 'GSMS项目', 1

-- API响应返回字符串
-- status: "NOT_STARTED"
```

## 技术要点总结

### 成功经验

1. **@JsonValue + toString() 组合**：实现JSON序列化为name()，同时不影响数据库存储
2. **显式typeHandler**：自定义SQL必须显式声明，不能依赖全局配置
3. **单对象参数**：避免对象属性路径导致的类型推断问题
4. **渐进式改造**：以User模块为示例，验证后推广到其他模块

### 经验教训

1. **不要修改业务代码**：应该修改测试用例来匹配业务代码，而非反向操作
2. **逐模块验证**：完成一个模块测试通过后，再进行下一个模块
3. **查阅官方文档**：MyBatis-Plus 的枚举处理有特定机制，需要正确理解

### 关键配置

```yaml
# application.yml
mybatis-plus:
  type-enums-package: com.gsms.gsms.model.enums
  configuration:
    default-enum-type-handler: com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler
```

```xml
<!-- Mapper XML -->
#{status, typeHandler=com.baomidou.mybatisplus.core.handlers.MybatisEnumTypeHandler}
```

```java
// Enum
@EnumValue
private final Integer code;

@JsonValue
@Override
public String toString() {
    return this.name();
}
```

## 参考资料

- MyBatis-Plus 官方文档 - 枚举处理
- Jackson @JsonValue 注解文档
- 相关Issue提交记录
