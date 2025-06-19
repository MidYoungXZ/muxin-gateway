package com.muxin.gateway.admin;

import cn.dev33.satoken.secure.BCrypt;
import org.junit.jupiter.api.Test;

/**
 * BCrypt密码测试
 * 
 * @author muxin
 */
public class BCryptTest {

    @Test
    public void testPasswordHash() {
        String plainPassword = "admin123";
        String dbHashedPassword = "$2a$10$5Z1Kbm99AbBFN7y8Dd3.V.UGmeJX8nWKG47aPXXMuupC7kLe8lKIu";
        
        System.out.println("原始密码: " + plainPassword);
        System.out.println("数据库密码hash: " + dbHashedPassword);
        
        // 验证密码
        boolean isValid = BCrypt.checkpw(plainPassword, dbHashedPassword);
        System.out.println("密码验证结果: " + isValid);
        
        // 生成新的密码hash
        String newHash = BCrypt.hashpw(plainPassword);
        System.out.println("新生成的密码hash: " + newHash);
        
        // 验证新生成的hash
        boolean isNewValid = BCrypt.checkpw(plainPassword, newHash);
        System.out.println("新hash验证结果: " + isNewValid);
    }
    
    @Test
    public void generateAdminPassword() {
        String password = "admin123";
        String hash = BCrypt.hashpw(password);
        System.out.println("========================================");
        System.out.println("管理员密码加密结果:");
        System.out.println("原始密码: " + password);
        System.out.println("加密后的密码: " + hash);
        System.out.println("========================================");
        
        // 验证生成的密码
        boolean isValid = BCrypt.checkpw(password, hash);
        System.out.println("验证结果: " + isValid);
    }
} 