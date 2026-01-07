package com.gsms.gsms;

import com.gsms.gsms.service.CacheService;
import com.gsms.gsms.service.impl.CacheServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 验证 Spring Bean 单例特性
 */
@SpringBootTest
class SpringSingletonTest {

    @Autowired
    private CacheService cacheService1;

    @Autowired
    private CacheService cacheService2;

    @Test
    void testSingleton() {
        // 验证两个注入的是同一个实例
        System.out.println("cacheService1: " + cacheService1.hashCode());
        System.out.println("cacheService2: " + cacheService2.hashCode());

        assertSame(cacheService1, cacheService2, "两个应该是同一个实例");
    }

    @Test
    void testFieldSharing() throws NoSuchFieldException, IllegalAccessException {
        // 通过反射获取内部字段
        Field userCacheField = CacheServiceImpl.class.getDeclaredField("userCache");
        userCacheField.setAccessible(true);

        Object userCache1 = userCacheField.get(cacheService1);
        Object userCache2 = userCacheField.get(cacheService2);

        System.out.println("userCache1: " + userCache1.hashCode());
        System.out.println("userCache2: " + userCache2.hashCode());

        assertSame(userCache1, userCache2, "两个应该是同一个 Map 实例");
    }

    @Test
    void explainWhyNotStatic() {
        System.out.println("=== 为什么不用 static 也能共享？ ===");
        System.out.println("1. Spring 默认 Bean 作用域是 Singleton（单例）");
        System.out.println("2. 整个应用只有一个 CacheServiceImpl 实例");
        System.out.println("3. 所有线程/请求使用的都是同一个实例");
        System.out.println("4. 所以实例的字段自然就是共享的");
        System.out.println();
        System.out.println("static vs 单例的区别：");
        System.out.println("- static: 类级别，该类所有实例共享");
        System.out.println("- 单例: 应用级别，整个应用只有一个实例");
        System.out.println();
        System.out.println("效果相同，但单例更灵活：");
        System.out.println("- 可以被 Spring 管理（依赖注入）");
        System.out.println("- 可以被 AOP 代理");
        System.out.println("- 可以有多个不同配置的实例（如果需要）");
    }
}
