package com.muxin.gateway.core.filter;

import com.muxin.gateway.core.factory.impl.StripPrefixFilterFactory;
import com.muxin.gateway.core.http.DefaultServerWebExchange;
import com.muxin.gateway.core.http.ServerWebExchange;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.HashMap;
import java.util.Map;

import static com.muxin.gateway.core.common.GatewayConstants.GATEWAY_STRIPPED_PATH_ATTR;
import static org.junit.jupiter.api.Assertions.*;

/**
 * StripPrefix过滤器测试
 */
@DisplayName("StripPrefix过滤器测试")
public class StripPrefixFilterTest {

    @Test
    @DisplayName("测试剥离2个路径段")
    void testStripTwoParts() {
        // 创建StripPrefix过滤器，剥离2个路径段
        StripPrefixFilterFactory factory = new StripPrefixFilterFactory();
        Map<String, String> args = new HashMap<>();
        args.put("parts", "2");
        RouteFilter filter = factory.create(args);

        // 创建测试请求：/api/test-server/git/version
        ServerWebExchange exchange = createExchange("/api/test-server/git/version");

        // 执行过滤器
        filter.filter(exchange);

        // 验证剥离后的路径
        String strippedPath = exchange.getAttribute(GATEWAY_STRIPPED_PATH_ATTR);
        assertNotNull(strippedPath);
        assertEquals("/git/version", strippedPath);
    }

    @Test
    @DisplayName("测试剥离1个路径段")
    void testStripOnePart() {
        StripPrefixFilterFactory factory = new StripPrefixFilterFactory();
        Map<String, String> args = new HashMap<>();
        args.put("parts", "1");
        RouteFilter filter = factory.create(args);

        ServerWebExchange exchange = createExchange("/test/hello/world");

        filter.filter(exchange);

        String strippedPath = exchange.getAttribute(GATEWAY_STRIPPED_PATH_ATTR);
        assertNotNull(strippedPath);
        assertEquals("/hello/world", strippedPath);
    }

    @Test
    @DisplayName("测试带查询参数的路径剥离")
    void testStripWithQueryParams() {
        StripPrefixFilterFactory factory = new StripPrefixFilterFactory();
        Map<String, String> args = new HashMap<>();
        args.put("parts", "2");
        RouteFilter filter = factory.create(args);

        ServerWebExchange exchange = createExchange("/api/test-server/git/version?param1=value1&param2=value2");

        filter.filter(exchange);

        String strippedPath = exchange.getAttribute(GATEWAY_STRIPPED_PATH_ATTR);
        assertNotNull(strippedPath);
        assertEquals("/git/version?param1=value1&param2=value2", strippedPath);
    }

    @Test
    @DisplayName("测试剥离过多路径段的情况")
    void testStripTooManyParts() {
        StripPrefixFilterFactory factory = new StripPrefixFilterFactory();
        Map<String, String> args = new HashMap<>();
        args.put("parts", "5"); // 剥离5个路径段，但实际只有2个
        RouteFilter filter = factory.create(args);

        ServerWebExchange exchange = createExchange("/api/user");

        filter.filter(exchange);

        String strippedPath = exchange.getAttribute(GATEWAY_STRIPPED_PATH_ATTR);
        assertNotNull(strippedPath);
        assertEquals("/", strippedPath); // 应该返回根路径
    }

    /**
     * 创建测试用的ServerWebExchange
     */
    private ServerWebExchange createExchange(String uri) {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
            HttpVersion.HTTP_1_1, 
            HttpMethod.GET, 
            uri
        );
        
        return DefaultServerWebExchange.fromNettyRequest(request, null);
    }
} 