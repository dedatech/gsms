package com.gsms.gsms;

import com.gsms.gsms.infra.config.CorsProperties;
import com.gsms.gsms.infra.config.JwtProperties;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Spring Boot 启动类
 */
@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, CorsProperties.class})
@MapperScan("com.gsms.gsms.repository")
public class GsmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(GsmsApplication.class, args);
    }

}