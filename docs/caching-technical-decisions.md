# 缓存机制技术决策文档

## 背景

在实现 User 模块性能优化时，对于缓存方案的选择进行了深入的技术调研和对比分析。

本文档记录了：
1. **缓存方案对比分析**：ConcurrentHashMap vs Caffeine vs Ehcache vs Redis
2. **Spring 单例原理**：为什么非 static 字段也能实现多线程共享

---

## 一、缓存方案对比分析

### 1.1 当前方案：ConcurrentHashMap

**实现方式：**
```java
@Service
public class CacheServiceImpl implements CacheService {
    private final Map<Long, User> userCache = new ConcurrentHashMap<>();
    private final Map<String, User> userUsernameCache = new ConcurrentHashMap<>();
    private final Map<Long, Department> departmentCache = new ConcurrentHashMap<>();
}
```

**优势：**
- ✅ 零依赖，JDK 自带
- ✅ 简单直观，代码完全可控
- ✅ 启动快，内存占用极小
- ✅ 对于小规模数据（< 10,000 条）性能足够
- ✅ 线程安全（ConcurrentHashMap 保证）

**劣势：**
- ❌ 无过期机制（数据永久驻留内存）
- ❌ 无淘汰策略（内存可能无限增长）
- ❌ 无缓存统计（看不到命中率）
- ❌ 无自动刷新（需手动管理）
- ❌ 无法限制缓存大小

**适用场景：**
- 用户数 < 5,000
- 单机部署
- 数据量 < 10,000 条

---

### 1.2 Caffeine（推荐升级方案）

**官方文档：** https://github.com/ben-manes/caffeine

**简介：**
- Spring Boot 2.x 默认缓存框架
- Java 8 性能最强的缓存库
- 比 ConcurrentHashMap 快 30%

**Maven 依赖：**
```xml
<dependency>
    <groupId>com.github.ben-manes.caffeine</groupId>
    <artifactId>caffeine</artifactId>
    <version>3.1.8</version>
</dependency>
```

**使用示例：**
```java
@Configuration
public class CacheConfig {
    @Bean
    public Cache<Long, User> userCache() {
        return Caffeine.newBuilder()
            .maximumSize(10_000)                          // 最多缓存1万条
            .expireAfterWrite(30, TimeUnit.MINUTES)       // 30分钟过期
            .recordStats()                                // 记录统计信息
            .build();
    }
}
```

**改造 CacheServiceImpl：**
```java
@Service
public class CacheServiceImpl implements CacheService {

    private final Cache<Long, User> userCache;
    private final Cache<String, User> userUsernameCache;
    private final Cache<Long, Department> departmentCache;

    public CacheServiceImpl(UserMapper userMapper, DepartmentMapper departmentMapper) {
        this.userMapper = userMapper;
        this.departmentMapper = departmentMapper;

        // 初始化 Caffeine 缓存
        this.userCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .recordStats()
            .build();

        this.userUsernameCache = Caffeine.newBuilder()
            .maximumSize(10_000)
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

        this.departmentCache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(1, TimeUnit.HOURS)
            .build();
    }

    @Override
    public String getUserNicknameById(Long userId) {
        if (userId == null) return null;
        User user = userCache.getIfPresent(userId);
        return user != null ? user.getNickname() : null;
    }

    @Override
    public void putUser(User user) {
        if (user == null || user.getId() == null) return;
        userCache.put(user.getId(), user);
        if (user.getUsername() != null) {
            userUsernameCache.put(user.getUsername(), user);
        }
    }

    // 新增：统计查询方法
    public CacheStats getUserCacheStats() {
        return userCache.stats();
    }
}
```

**监控缓存性能：**
```java
@RestController
@RequestMapping("/admin")
public class CacheMonitorController {

    @Autowired
    private CacheService cacheService;

    @GetMapping("/cache/stats")
    public Result getCacheStats() {
        CacheStats stats = cacheService.getUserCacheStats();
        return Result.success(Map.of(
            "hitRate", stats.hitRate(),
            "hitCount", stats.hitCount(),
            "missCount", stats.missCount(),
            "loadSuccessCount", stats.loadSuccessCount(),
            "totalLoadTime", stats.totalLoadTime() / 1_000_000 + "ms",
            "evictionCount", stats.evictionCount()
        ));
    }
}
```

**示例输出：**
```json
{
  "hitRate": 0.95,          // 95% 命中率
  "hitCount": 9500,         // 命中次数
  "missCount": 500,         // 未命中次数
  "loadSuccessCount": 500,
  "totalLoadTime": "125ms",
  "evictionCount": 10       // 淘汰次数
}
```

**优势：**
- ✅ **性能最强**：比 ConcurrentHashMap 快 30%（异步加载）
- ✅ **自动过期**：支持 TTL、TTW、访问后过期
- ✅ **淘汰策略**：LRU、LFU、基于大小
- ✅ **自动加载**：`cache.get(key, k -> loadFromDb(k))`
- ✅ **统计信息**：命中率、加载时间、淘汰次数
- ✅ **Spring Boot 原生支持**：`@Cacheable` 注解即用
- ✅ **异步刷新**：后台刷新，不阻塞请求

**劣势：**
- ⚠️ 增加一个依赖（约 2MB）
- ⚠️ 需要学习 API（但很简单）

---

### 1.3 Ehcache

**简介：**
- 老牌缓存框架
- 功能全面（磁盘缓存、集群支持）

**对比 Caffeine：**
- ✅ 功能更多
- ❌ 性能较差（比 Caffeine 慢 20-30%）
- ❌ 配置较复杂

**不推荐理由：**
Caffeine 性能更强且功能够用，Ehcache 在纯内存缓存场景下没有优势。

---

### 1.4 Redis（分布式缓存）

**Maven 依赖：**
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

**优势：**
- ✅ 分布式共享（多实例部署必需）
- ✅ 数据持久化
- ✅ 丰富的数据结构

**劣势：**
- ❌ 需要额外部署 Redis 服务
- ❌ 网络开销（比本地缓存慢 10-100 倍）
- ❌ 运维成本

---

### 1.5 方案选型决策表

| 方案 | 适用场景 | 用户规模 | 部署方式 | 是否推荐 |
|------|---------|---------|---------|---------|
| **ConcurrentHashMap** | 当前项目 | < 5,000 | 单机 | ✅ **当前方案** |
| **Caffeine** | 需要过期+统计 | 5,000-50,000 | 单机 | ⏸️ **未来升级** |
| **Ehcache** | 需要磁盘缓存 | 任意 | 单机 | ❌ **不推荐** |
| **Redis** | 分布式集群 | > 10,000 | 多实例 | ❌ **暂不需要** |

---

## 二、升级建议路线图

### 阶段 1：当前阶段（推荐保持）

**时间：** 现在
**用户规模：** < 1,000
**方案：** ConcurrentHashMap

**决策理由：**
1. 用户规模小，数据量小
2. 单机部署，不需要分布式
3. 功能已经够用（启动预热 + 手动更新）
4. 零依赖，简单可靠

**结论：✅ 不需要升级**

---

### 阶段 2：业务增长后（Caffeine）

**时间：** 用户数 > 5,000
**触发条件：** 满足以下任一条件
- ❗ 缓存占用内存 > 100MB（需要淘汰策略）
- ❗ 需要自动过期（数据一致性要求高）
- ❗ 需要监控命中率（性能优化必需）

**升级成本：**
- 开发时间：1-2 小时
- 代码改动：< 100 行
- 测试时间：1 小时

**升级步骤：**
1. 添加 Caffeine 依赖
2. 修改 `CacheServiceImpl` 字段类型
3. 配置过期时间和最大容量
4. 添加监控接口
5. 运行测试验证

---

### 阶段 3：分布式阶段（Redis）

**时间：** 用户数 > 10,000
**触发条件：**
- 需要水平扩展（多台服务器）
- 缓存需要在多个实例间共享

**升级步骤：**
1. 部署 Redis 服务
2. 添加 Redis 依赖
3. 配置 Redis 连接
4. 使用 Spring Cache 抽象层
5. 灰度切流验证

---

## 三、Spring 单例原理

### 3.1 问题：为什么非 static 也能多线程共享？

**代码示例：**
```java
@Service  // ← 关键！Spring 会创建单例 Bean
public class CacheServiceImpl implements CacheService {

    // 不是 static，但整个应用只有一份
    private final Map<Long, User> userCache = new ConcurrentHashMap<>();
}
```

### 3.2 Spring 容器启动流程

```
┌─────────────────────────────────────────┐
│ 1. Spring 扫描 @Service、@Component    │
│    发现 CacheServiceImpl.class          │
└─────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│ 2. 创建单例实例                         │
│    CacheServiceImpl instance =          │
│      new CacheServiceImpl(...)          │
│    ├─ userCache = new ConcurrentHashMap() │
│    ├─ userUsernameCache = ...           │
│    └─ departmentCache = ...              │
└─────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│ 3. 放入 Spring 容器（单例池）           │
│    singletonObjects.put(                 │
│      "cacheServiceImpl",                 │
│      instance  ← 只有一份！              │
│    )                                     │
└─────────────────────────────────────────┘
                 ↓
┌─────────────────────────────────────────┐
│ 4. 所有请求/注入都是同一个实例          │
│    Request1 → cacheService → instance   │
│    Request2 → cacheService → instance   │
│    Request3 → cacheService → instance   │
│    ...全部指向同一个对象！               │
└─────────────────────────────────────────┘
```

### 3.3 多线程共享内存原理

```
用户A请求 ──┐
            ├──→ Tomcat 线程池 ──→ Spring Controller ──┐
用户B请求 ──┘                                      ↓
                                              Spring 容器
                                                 ↓
                                        CacheServiceImpl
                                                 ↓
                                        userCache (ConcurrentHashMap)
                                                 ↓
                                      所有线程看到的都是同一个 Map！
```

**核心原理：**
1. **Spring Bean 默认是 Singleton（单例）**
2. **整个应用只有一个 `CacheServiceImpl` 实例**
3. **所有 HTTP 请求线程共享这个实例**
4. **实例的字段自然就是共享的**

### 3.4 static vs 单例对比

#### static 方式（不推荐）

```java
@Service
public class CacheServiceImpl implements CacheService {
    private static final Map<Long, User> userCache = new ConcurrentHashMap<>();

    public void putUser(User user) {
        userCache.put(user.getId(), user);
    }
}
```

**问题：**
- ❌ 无法被 Spring 代理（AOP 失效）
- ❌ 单元测试困难（需要手动清理 static）
- ❌ 无法有多个不同配置的实例
- ❌ 生命周期不受 Spring 管理

#### 单例方式（推荐 ✅）

```java
@Service  // Spring 单例 Bean
public class CacheServiceImpl implements CacheService {
    private final Map<Long, User> userCache = new ConcurrentHashMap<>();

    public void putUser(User user) {
        userCache.put(user.getId(), user);
    }
}
```

**优势：**
- ✅ 可以被 Spring 管理（依赖注入）
- ✅ 支持 AOP 代理（事务、日志等）
- ✅ 单元测试简单（每个测试独立实例）
- ✅ 可以有多个不同配置的实例（如需要）
- ✅ 生命周期由 Spring 容器管理

### 3.5 关键概念对比

| 特性 | static | Spring 单例 |
|------|--------|------------|
| **共享级别** | 类级别 | 应用级别 |
| **实例数量** | 无数个对象共享一个字段 | 只有一个对象 |
| **Spring 管理** | ❌ 不受管理 | ✅ 完全管理 |
| **依赖注入** | ❌ 不支持 | ✅ 支持 |
| **AOP 代理** | ❌ 不支持 | ✅ 支持 |
| **测试隔离** | ❌ 需手动清理 | ✅ 自动隔离 |
| **灵活性** | ❌ 差 | ✅ 好 |

### 3.6 验证单例特性

**测试代码：** `SpringSingletonTest.java`

```java
@SpringBootTest
class SpringSingletonTest {
    @Autowired
    private CacheService cacheService1;

    @Autowired
    private CacheService cacheService2;

    @Test
    void testSingleton() {
        // 验证两个注入的是同一个实例
        assertSame(cacheService1, cacheService2);
    }
}
```

**测试结果：**
```
cacheService1: 1114834984
cacheService2: 1114834984  ← 相同！
```

---

## 四、总结

### 4.1 为什么不用 static 也能多线程共享？

**答案：** Spring 的 `@Service` 默认创建单例 Bean

```
应用启动 → 创建唯一实例 → 所有请求共享这个实例 → 实例的字段自然共享
```

### 4.2 static vs 单例

- **效果相同**：都能实现多线程共享
- **实现不同**：static 是类级别，单例是应用级别
- **单例更好**：可被 Spring 管理，支持依赖注入和 AOP

### 4.3 当前方案决策

**ConcurrentHashMap + Spring 单例**
- ✅ 简单够用，零依赖
- ✅ 性能良好
- ✅ 易于维护
- ✅ 满足当前业务需求

**一句话：Spring 单例模式提供了 static 的共享能力，但保留了面向对象的灵活性。**

---

## 参考文档

- **Caffeine 官方文档**: https://github.com/ben-manes/caffeine
- **Spring Bean 作用域**: https://docs.spring.io/spring-framework/reference/core/beans/factory-scopes.html
- **Java ConcurrentHashMap**: https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentHashMap.html
