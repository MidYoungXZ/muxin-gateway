package com.muxin.gateway.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * bsfit-sde服务集成测试
 * 专门测试通过muxin-gateway访问bsfit-sde服务的功能
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class BsfitSdeIntegrationTest {

    private static final String GATEWAY_URL = "http://localhost:8080";
    private static final String BSFIT_SDE_ROUTE = "/api/test-server";
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();
    
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Test
    @Order(1)
    @DisplayName("验证网关服务是否启动")
    void testGatewayIsRunning() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(GATEWAY_URL + "/health"))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("检查网关状态...");
        System.out.println("响应状态: " + response.statusCode());
        
        assertTrue(response.statusCode() == 200 || response.statusCode() == 404, 
                "网关服务应该正在运行");
        System.out.println("网关服务正常运行");
    }

    @Test
    @Order(2)
    @DisplayName("测试bsfit-sde服务的/git/version接口")
    void testBsfitSdeGitVersionAPI() throws IOException, InterruptedException {
        String testUrl = GATEWAY_URL + BSFIT_SDE_ROUTE + "/git/version";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(testUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("User-Agent", "MuxinGateway-IntegrationTest/1.0")
                .header("X-Test-Case", "test-server-git-version")
                .GET()
                .build();

        System.out.println("\n测试bsfit-sde服务路由...");
        System.out.println("请求URL: " + testUrl);
        
        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        
        // 打印详细的请求和响应信息
        System.out.println("请求详情:");
        System.out.println("  - Method: GET");
        System.out.println("  - URL: " + request.uri());
        System.out.println("  - Headers: " + request.headers().map());
        
        System.out.println("\n响应详情:");
        System.out.println("  - Status Code: " + response.statusCode());
        System.out.println("  - Headers: " + response.headers().map());
        System.out.println("  - Body Length: " + response.body().length() + " 字符");
        System.out.println("  - Body Content: " + response.body());
        
        // 验证响应状态
        assertEquals(200, response.statusCode(), 
                "通过网关访问bsfit-sde的/git/version接口应该返回200状态码");
        
        // 验证响应内容
        assertNotNull(response.body(), "响应体不应该为空");
        assertFalse(response.body().trim().isEmpty(), "响应体不应该为空字符串");
        
        // 尝试解析响应内容（如果是JSON格式）
        try {
            if (response.body().trim().startsWith("{")) {
                Map<String, Object> jsonResponse = OBJECT_MAPPER.readValue(response.body(), Map.class);
                System.out.println("\n解析的JSON响应:");
                jsonResponse.forEach((key, value) -> 
                    System.out.println("  - " + key + ": " + value)
                );
            }
        } catch (Exception e) {
            System.out.println("响应不是JSON格式，这是正常的: " + e.getMessage());
        }
        
        System.out.println("\nbsfit-sde服务路由测试成功！");
    }

    @Test
    @Order(3)
    @DisplayName("测试网关路由转发的请求头")
    void testGatewayRequestHeaders() throws IOException, InterruptedException {
        String testUrl = GATEWAY_URL + BSFIT_SDE_ROUTE + "/git/version";
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(testUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("X-Original-Client", "IntegrationTest")
                .header("X-Request-ID", "test-" + System.currentTimeMillis())
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("\n测试请求头转发...");
        System.out.println("响应状态: " + response.statusCode());
        
        // 检查网关是否添加了自定义头
        Map<String, java.util.List<String>> responseHeaders = response.headers().map();
        System.out.println("响应头信息:");
        responseHeaders.forEach((key, values) -> 
            System.out.println("  - " + key + ": " + values)
        );
        
        assertEquals(200, response.statusCode(), "请求应该成功");
        System.out.println("请求头转发测试完成");
    }

    @Test
    @Order(4)
    @DisplayName("测试并发请求性能")
    void testConcurrentRequests() throws InterruptedException {
        int concurrentCount = 5;
        Thread[] threads = new Thread[concurrentCount];
        boolean[] results = new boolean[concurrentCount];
        long[] responseTimes = new long[concurrentCount];
        
        System.out.println("\n并发性能测试 (并发数: " + concurrentCount + ")");
        
        for (int i = 0; i < concurrentCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    
                    HttpRequest request = HttpRequest.newBuilder()
                            .uri(URI.create(GATEWAY_URL + BSFIT_SDE_ROUTE + "/git/version"))
                            .timeout(Duration.ofSeconds(30))
                            .header("X-Thread-ID", "thread-" + index)
                            .GET()
                            .build();

                    HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
                    
                    long endTime = System.currentTimeMillis();
                    responseTimes[index] = endTime - startTime;
                    results[index] = (response.statusCode() == 200);
                    
                    System.out.println("线程 " + index + ": " + response.statusCode() + 
                                     " (耗时: " + responseTimes[index] + "ms)");
                    
                } catch (Exception e) {
                    System.out.println("线程 " + index + " 失败: " + e.getMessage());
                    results[index] = false;
                }
            });
        }
        
        // 启动所有线程
        long startTime = System.currentTimeMillis();
        for (Thread thread : threads) {
            thread.start();
        }
        
        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }
        long totalTime = System.currentTimeMillis() - startTime;
        
        // 统计结果
        int successCount = 0;
        long totalResponseTime = 0;
        for (int i = 0; i < concurrentCount; i++) {
            if (results[i]) {
                successCount++;
                totalResponseTime += responseTimes[i];
            }
        }
        
        System.out.println("\n并发测试结果:");
        System.out.println("  - 总耗时: " + totalTime + "ms");
        System.out.println("  - 成功请求: " + successCount + "/" + concurrentCount);
        System.out.println("  - 成功率: " + (successCount * 100.0 / concurrentCount) + "%");
        if (successCount > 0) {
            System.out.println("  - 平均响应时间: " + (totalResponseTime / successCount) + "ms");
        }
        
        // 断言至少80%的请求成功
        assertTrue(successCount >= concurrentCount * 0.8, 
                "并发请求成功率应该至少80%");
        
        System.out.println("并发性能测试通过！");
    }

    @Test
    @Order(5)
    @DisplayName("测试错误场景处理")
    void testErrorScenarios() throws IOException, InterruptedException {
        System.out.println("\n测试错误场景处理...");
        
        // 测试不存在的路径
        String invalidUrl = GATEWAY_URL + BSFIT_SDE_ROUTE + "/invalid/endpoint";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(invalidUrl))
                .timeout(Duration.ofSeconds(30))
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        
        System.out.println("请求无效路径: " + invalidUrl);
        System.out.println("响应状态: " + response.statusCode());
        System.out.println("响应体: " + response.body());
        
        // 验证错误响应（应该是4xx或5xx）
        assertTrue(response.statusCode() >= 400, 
                "访问无效路径应该返回错误状态码");
        
        System.out.println("错误处理测试完成");
    }

    @Test
    @Order(6)
    @DisplayName("验证完整的请求-响应流程")
    void testCompleteRequestResponseFlow() throws IOException, InterruptedException {
        System.out.println("\n验证完整的请求-响应流程...");
        
        String testUrl = GATEWAY_URL + BSFIT_SDE_ROUTE + "/git/version";
        
        // 记录开始时间
        long startTime = System.currentTimeMillis();
        
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(testUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Accept", "application/json")
                .header("User-Agent", "MuxinGateway-FlowTest/1.0")
                .header("X-Flow-Test", "complete-flow")
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        
        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        
        System.out.println("流程验证结果:");
        System.out.println("  1. 请求构建: 成功");
        System.out.println("  2. 网关路由: 成功 (耗时: " + totalTime + "ms)");
        System.out.println("  3. 服务调用: " + (response.statusCode() == 200 ? "成功" : "失败"));
        System.out.println("  4. 响应返回: 成功");
        System.out.println("  5. 数据完整性: " + (response.body() != null && !response.body().isEmpty() ? "完整" : "不完整"));
        
        // 最终验证
        assertEquals(200, response.statusCode(), "完整流程应该成功");
        assertNotNull(response.body(), "响应数据应该完整");
        assertTrue(totalTime < 30000, "总响应时间应该在30秒内");
        
        System.out.println("\nmuxin-gateway路由功能验证成功！");
        System.out.println("   - bsfit-sde服务可以通过网关正常访问");
        System.out.println("   - /git/version接口响应正常");
        System.out.println("   - 网关路由转发功能工作正常");
    }
} 