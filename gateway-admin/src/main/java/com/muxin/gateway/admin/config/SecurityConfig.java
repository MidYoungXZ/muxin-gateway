package com.muxin.gateway.admin.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 安全配置类.
 * <p>
 * 该配置类负责应用的安全相关配置，主要用于密码加密。
 * 使用BCrypt算法进行密码编码，提供安全的密码存储方案。
 * </p>
 *
 * @author muxin
 * @version 1.0.0
 * @since 1.0.0

 */
@Configuration
public class SecurityConfig {

    /**
     * 配置密码编码器.
     * <p>
     * 使用BCrypt算法对密码进行编码，BCrypt是一种自适应的哈希函数，
     * 基于Blowfish加密算法，专为密码存储设计，具有内置的盐值机制。
     * </p>
     *
     * @return PasswordEncoder实例，使用BCrypt算法
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}