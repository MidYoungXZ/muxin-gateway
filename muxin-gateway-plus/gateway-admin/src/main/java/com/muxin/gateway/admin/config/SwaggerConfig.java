package com.muxin.gateway.admin.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger配置类
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI gatewayAdminOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Muxin Gateway Admin API")
                        .description("Muxin Gateway管理接口文档")
                        .version("v1.0.0")
                        .license(new License().name("Apache 2.0").url("https://www.apache.org/licenses/LICENSE-2.0"))
                        .contact(new Contact().name("Muxin").url("https://github.com/your-username").email("your-email@example.com")))
                .externalDocs(new ExternalDocumentation()
                        .description("Muxin Gateway文档")
                        .url("https://github.com/your-username/muxin-gateway"))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("GATEWAY_SESSION_ID")))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"))
                .servers(List.of(
                    new io.swagger.v3.oas.models.servers.Server()
                        .url("/")
                        .description("默认服务器")
                ));
    }
} 