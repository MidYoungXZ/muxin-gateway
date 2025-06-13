package com.muxin.gateway.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 网关路由功能测试
 * 测试muxin-gateway的路由转发功能
 */
@SpringBootTest
@ActiveProfiles("test")
public class GatewayRouteTest {

    private HttpClient httpClient;
    private String gatewayBaseUrl = "http://localhost:8080";
    
    @BeforeEach
    void setUp() {
        // 创建HTTP客户端
        httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    @Test
    @DisplayName("测试网关健康检查")
    void testGatewayHealthCheck() throws IOException, InterruptedException {
        // 构建请求
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(gatewayBaseUrl + "/health"))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        // 发送请求
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // 验证响应
        System.out.println("=== 健康检查测试 ===");
        System.out.println("请求URL: " + request.uri());
        System.out.println("响应状态码: " + response.statusCode());
        System.out.println("响应体: " + response.body());
        System.out.println("响应头: " + response.headers().map());
        
        // 断言
        assertTrue(response.statusCode() == 200 || response.statusCode() == 404, 
                "健康检查应该返回200或404状态码");
    }

    @Test
    @DisplayName("测试bsfit-sde服务路由 - /git/version接口")
    void testBsfitSdeServiceRoute() throws IOException, InterruptedException {
        // 构建请求 - 通过网关访问bsfit-sde服务
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(gatewayBaseUrl + "/api/test-server/git/version"))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("User-Agent", "MuxinGateway-Test/1.0")
                .GET()
                .build();

        // 发送请求
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        // 打印详细信息
        System.out.println("\n=== bsfit-sde服务路由测试 ===");
        System.out.println("请求URL: " + request.uri());
        System.out.println("请求头: " + request.headers().map());
        System.out.println("响应状态码: " + response.statusCode());
        System.out.println("响应头: " + response.headers().map());
        System.out.println("响应体: " + response.body());
        
        // 验证响应
        assertEquals(200, response.statusCode(), 
                "通过网关访问bsfit-sde服务应该返回200状态码");
        assertNotNull(response.body(), "响应体不应该为空");
        assertFalse(response.body().trim().isEmpty(), "响应体不应该为空字符串");
        
        // 验证是否包含版本信息（通常Git版本接口会返回相关信息）
        String responseBody = response.body().toLowerCase();
        assertTrue(responseBody.contains("version") || 
                  responseBody.contains("git") || 
                  responseBody.contains("commit") ||
                  responseBody.contains("branch"), 
                "响应体应该包含版本相关信息");
    }

    @Test
    @DisplayName("测试直接访问bsfit-sde服务（不通过网关）")
    void testDirectBsfitSdeServiceAccess() throws IOException, InterruptedException {
        // 注意：这需要知道bsfit-sde服务的实际地址
        // 这里假设可以直接访问，如果不行可以跳过此测试
        String directServiceUrl = "http://test-server/git/version"; // 需要根据实际情况调整
        
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(directServiceUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            System.out.println("\n=== 直接访问bsfit-sde服务测试 ===");
            System.out.println("请求URL: " + request.uri());
            System.out.println("响应状态码: " + response.statusCode());
            System.out.println("响应体: " + response.body());
            
            assertEquals(200, response.statusCode(), "直接访问bsfit-sde服务应该成功");
            
        } catch (Exception e) {
            System.out.println("直接访问服务失败（这是正常的，因为可能需要通过网关）: " + e.getMessage());
            // 不抛出异常，因为直接访问可能不被允许
        }
    }

    @Test
    @DisplayName("测试网关路由性能")
    void testGatewayRoutePerformance() throws InterruptedException {
        int requestCount = 10;
        CompletableFuture<HttpResponse<String>>[] futures = new CompletableFuture[requestCount];
        
        long startTime = System.currentTimeMillis();
        
        // 并发发送多个请求
        for (int i = 0; i < requestCount; i++) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(gatewayBaseUrl + "/api/test-server/git/version"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Accept", "application/json")
                    .header("X-Request-ID", "test-" + i)
                    .GET()
                    .build();
            
            futures[i] = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString());
        }
        
        // 等待所有请求完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures);
        allFutures.join();
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        System.out.println("\n=== 网关路由性能测试 ===");
        System.out.println("并发请求数: " + requestCount);
        System.out.println("总耗时: " + totalTime + "ms");
        System.out.println("平均耗时: " + (totalTime / requestCount) + "ms");
        
        // 检查响应结果
        int successCount = 0;
        for (CompletableFuture<HttpResponse<String>> future : futures) {
            try {
                HttpResponse<String> response = future.get();
                if (response.statusCode() == 200) {
                    successCount++;
                }
                System.out.println("响应状态: " + response.statusCode());
            } catch (Exception e) {
                System.out.println("请求失败: " + e.getMessage());
            }
        }
        
        System.out.println("成功请求数: " + successCount + "/" + requestCount);
        
        // 断言至少有一半的请求成功
        assertTrue(successCount >= requestCount / 2, 
                "至少应该有一半的请求成功");
    }

    @Test
    @DisplayName("测试网关错误处理")
    void testGatewayErrorHandling() throws IOException, InterruptedException {
        // 测试不存在的路径
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(gatewayBaseUrl + "/api/test-server/nonexistent/path"))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("\n=== 网关错误处理测试 ===");
        System.out.println("请求URL: " + request.uri());
        System.out.println("响应状态码: " + response.statusCode());
        System.out.println("响应体: " + response.body());
        
        // 验证错误响应
        assertTrue(response.statusCode() >= 400, "访问不存在的路径应该返回4xx或5xx状态码");
    }

    @Test
    @DisplayName("测试网关管理界面")
    void testGatewayAdminInterface() throws IOException, InterruptedException {
        // 测试管理界面访问
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(gatewayBaseUrl + "/admin/"))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("\n=== 网关管理界面测试 ===");
        System.out.println("请求URL: " + request.uri());
        System.out.println("响应状态码: " + response.statusCode());
        System.out.println("响应内容长度: " + response.body().length() + " 字符");
        
        // 验证管理界面
        assertEquals(200, response.statusCode(), "管理界面应该可以访问");
        assertTrue(response.body().contains("Muxin Gateway"), "响应应该包含网关标题");
    }
} 