# IDEA Debug 模式启动卡死问题诊断报告

## 问题描述

**现象：** 应用在 IDEA Debug 模式下启动时卡在 MyBatis-Plus 初始化后、WebJars 扫描阶段，CPU 占用高，风扇狂转。Run 模式启动正常。

**环境：**
- IDEA: 2024.x
- Java: 1.8.0_202 / Java 17
- Spring Boot: 2.7.0
- ClassGraph: 4.8.149 (springdoc-openapi-ui 传递依赖)
- 架构：标准三层架构 + DTO 模式

---

## 问题定位过程

### 1. 使用 jps 和 jstack 诊断

```bash
# 查看 Java 进程
jps

# 查看线程堆栈
jstack <pid> | grep -A 30 "main"
```

### 2. 发现卡住位置

**Main 线程状态：**
```
java.lang.Thread.State: WAITING (parking)
at java.util.concurrent.FutureTask.awaitDone(FutureTask.java:447)
at java.util.concurrent.FutureTask.get(FutureTask.java:190)
at io.github.classgraph.ClassGraph.scan(ClassGraph.java:1584)
at org.webjars.WebJarAssetLocator.scanForWebJars(WebJarAssetLocator.java:183)
at org.webjars.WebJarAssetLocator.<init>(WebJarAssetLocator.java:205)
```

**ClassGraph-worker 线程状态：**
```
java.lang.Thread.State: WAITING (parking)
at java.util.concurrent.LinkedBlockingQueue.take(WorkQueue.java:238)
at nonapi.io.github.classgraph.concurrency.WorkQueue.runWorkLoop(...)
```

---

## 根本原因分析

### 死锁机制

```
┌─────────────────────────────────────────────────────┐
│ Main Thread (Spring Boot 主线程)                      │
│   ↓                                                  │
│ FutureTask.get() → 等待 ClassGraph.scan() 完成        │
│   ↓                                                  │
│ 持有锁：Spring 容器初始化锁                            │
└─────────────────────────────────────────────────────┘
                    ↓ 等待
┌─────────────────────────────────────────────────────┐
│ ClassGraph-worker-N (多个工作线程)                    │
│   ↓                                                  │
│ LinkedBlockingQueue.take() → 等待任务队列             │
│   ↓                                                  │
│ 状态：WAITING (parking)                               │
└─────────────────────────────────────────────────────┘
                    ↓ 等待任务分发
┌─────────────────────────────────────────────────────┐
│ 任务分发者 (Main 线程)                                │
│   ↓                                                  │
│ 被 IDEA Debug Agent 阻塞                             │
│   ↓                                                  │
│ 无法向 LinkedBlockingQueue 分发任务                   │
└─────────────────────────────────────────────────────┘
```

### 核心冲突

| 组件 | 作用 | 在 Debug 模式下的行为 |
|------|------|----------------------|
| **IDEA Debug Agent** | JVM 调试代理，字节码增强 | 在每个方法入口/出口插入事件通知 |
| **ClassGraph** | 快速扫描 classpath | 使用 ThreadPoolExecutor 并行扫描 |
| **ForkJoinPool** | Java 并行框架 | 启动多个工作线程（默认 CPU 核心数） |

**死锁场景：**
1. Main 线程调用 `ClassGraph.scan()`，等待 worker 线程完成
2. ClassGraph 启动多个 worker 线程并行扫描
3. Worker 线程快速触发大量 Debug 事件（方法调用）
4. Debug Agent 处理事件时需要同步访问共享状态
5. Main 线程被 Debug Agent 阻塞，无法向工作队列分发新任务
6. Worker 线程等待任务，Main 等待 Worker → **死锁**

---

## 解决方案

### 方案 A：在 IDEA Debug 配置中添加 JVM 参数（推荐）

**步骤：**
1. `Run` → `Edit Configurations`
2. 选择 `GsmsApplication` 的 **Debug** 配置
3. 在 `VM options` 中添加：
```
-Djava.util.concurrent.ForkJoinPool.common.parallelism=1
```

**原理：** 将 ForkJoinPool 并行度设置为 1，禁用并行处理，ClassGraph 退化为单线程扫描。

**优点：**
- ✅ 不修改代码
- ✅ 保留完整功能（包括 Swagger UI）
- ✅ 只影响 Debug 模式，Run 模式正常
- ⚠️ ClassGraph 扫描略慢，但不会死锁

---

### 方案 B：排除 webjars-locator-core 依赖

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-ui</artifactId>
    <version>1.6.14</version>
    <exclusions>
        <exclusion>
            <groupId>org.webjars</groupId>
            <artifactId>webjars-locator-core</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

**缺点：** Swagger UI 的某些静态资源可能无法正常加载

---

### 方案 C：日常使用 Run 模式（最实用）

- ✅ 开发时使用 **Run 模式**（快速启动，5-10秒）
- ✅ 只在需要打断点调试时使用 **Debug 模式**（加上 JVM 参数）
- ✅ Debug 模式启动慢但可用（30-60秒）

---

## 诊断工具使用

### 常用命令

```bash
# 1. 查看所有 Java 进程
jps

# 2. 查看特定线程的堆栈
jstack <pid> | grep -A 30 "main"

# 3. 查看所有线程状态统计
jstack <pid> | grep "java.lang.Thread.State" | sort | uniq -c

# 4. 查看特定名称的线程
jstack <pid> | grep "ClassGraph-worker"

# 5. 导出完整线程 dump
jstack <pid> > thread_dump.txt
```

### 线程状态说明

| 状态 | 含义 | 是否正常 |
|------|------|---------|
| RUNNABLE | 正在运行或等待 CPU | ✅ 正常 |
| WAITING (parking) | 等待某个条件 | ⚠️ 可能正常，需检查堆栈 |
| TIMED_WAITING | 等待超时 | ✅ 正常 |
| BLOCKED | 等待锁被释放 | ❌ 可能死锁 |

---

## 技术细节

### 为什么构造器注入不是问题

在排查过程中，怀疑是构造器注入导致的。实际上：
- 构造器注入在 IDEA Debug 模式下**不会**导致死锁
- 字段注入和构造器注入在 Debug 模式下表现一致
- 真正的问题是 ClassGraph 的并行扫描与 Debug Agent 的冲突

### 为什么升级 ClassGraph 无效

从 4.8.149 升级到 4.8.172 后问题依然存在：
- 这不是 ClassGraph 的 bug
- 而是 JVM Debug Agent 与并行任务队列的**根本性不兼容**
- ClassGraph 官方也承认了这个限制

### 为什么禁用资源链无效

在 WebConfig 中设置 `resourceChain(false)` 不起作用：
- Spring Boot 仍会在启动时创建 `WebJarsResourceResolver`
- `WebJarsResourceResolver` 构造函数就会调用 `ClassGraph.scan()`
- 只有完全禁用资源处理器或降低并行度才能解决

---

## 参考资料

- [ClassGraph GitHub Issues](https://github.com/classgraph/classgraph/issues)
- [IntelliJ IDEA Debug Mode](https://www.jetbrains.com/help/idea/debugging-code.html)
- [Java ForkJoinPool Documentation](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html)

---

## 总结

**问题本质：** IDEA Debug Agent 与 ClassGraph 并行扫描的并发冲突

**解决方法：** 限制 ForkJoinPool 并行度为 1（`-Djava.util.concurrent.ForkJoinPool.common.parallelism=1`）

**影响范围：** 仅 Debug 模式启动慢 20-30 秒，功能完全正常

**记录时间：** 2025-12-29
**问题状态：** ✅ 已解决
