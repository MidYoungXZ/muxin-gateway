package com.muxin.gateway.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Web配置类
 * 用于配置HTTP消息转换器，支持多种Content-Type
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * 配置内容协商
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer.defaultContentType(MediaType.APPLICATION_JSON)
                 .mediaType("json", MediaType.APPLICATION_JSON)
                 .mediaType("xml", MediaType.APPLICATION_XML)
                 .mediaType("html", MediaType.TEXT_HTML)
                 .mediaType("form", MediaType.APPLICATION_FORM_URLENCODED);
    }
    
    /**
     * 配置消息转换器
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // 添加表单消息转换器，处理application/x-www-form-urlencoded
        FormHttpMessageConverter formConverter = new FormHttpMessageConverter();
        converters.add(formConverter);
        
        // 添加JSON消息转换器，处理application/json
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                new MediaType("application", "*+json")
        ));
        converters.add(jsonConverter);
        
        // 添加字符串消息转换器，处理text/plain
        StringHttpMessageConverter stringConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        stringConverter.setSupportedMediaTypes(List.of(
                MediaType.TEXT_PLAIN,
                MediaType.TEXT_HTML
        ));
        converters.add(stringConverter);
    }
} 