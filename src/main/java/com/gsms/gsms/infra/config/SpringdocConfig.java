package com.gsms.gsms.infra.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

/**
 * Springdoc OpenAPI配置类
 */
@Configuration
public class SpringdocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        // 定义安全方案名称
        final String securitySchemeName = "Bearer Authentication";
        
        return new OpenAPI()
                .info(new Info()
                        .title("工时管理系统API文档")
                        .version("1.0")
                        .description("工时管理系统(GSMS) RESTful API接口文档\n\n" +
                                "**认证说明：**\n" +
                                "1. 先调用 `/api/users/login` 接口获取 JWT Token\n" +
                                "2. 点击右上角 'Authorize' 按钮\n" +
                                "3. 在弹出框中输入获取到的 Token（直接输入 Token 即可，无需添加 'Bearer ' 前缀）\n" +
                                "4. 点击 'Authorize' 确认，即可测试需要鉴权的接口")
                        .contact(new Contact()
                                .name("GSMS开发团队")
                                .url("https://github.com/gsms")
                                .email("gsms@example.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .servers(Arrays.asList(
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地开发服务器"),
                        new Server()
                                .url("https://api.gsms.com")
                                .description("生产服务器")))
                // 添加JWT认证配置
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("请输入JWT Token")))
                // 全局应用安全认证
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName));
    }
}