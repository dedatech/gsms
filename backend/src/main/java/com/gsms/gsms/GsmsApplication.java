package com.gsms.gsms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 启动类
 */
@SpringBootApplication
@MapperScan("com.gsms.gsms.repository")
public class GsmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GsmsApplication.class, args);
    }

}