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
 * Swagger配置类.
 * <p>
 * 该配置类用于集成Swagger/OpenAPI 3.0，自动生成API接口文档。
 * 主要配置包括：
 * <ul>
 *     <li>API文档的基本信息（标题、描述、版本、许可协议等）</li>
 *     <li>联系信息</li>
 *     <li>安全认证配置（Cookie认证）</li>
 *     <li>外部文档链接</li>
 *     <li>服务器配置</li>
 * </ul>
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0

 */
@Configuration
public class SwaggerConfig {

    /**
     * 配置OpenAPI文档信息.
     * <p>
     * 创建并配置OpenAPI实例，定义API文档的元数据和认证方式。
     * </p>
     *
     * @return OpenAPI实例，包含完整的API文档配置
     */
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