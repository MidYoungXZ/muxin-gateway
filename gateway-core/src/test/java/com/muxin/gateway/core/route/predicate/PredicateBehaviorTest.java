package com.muxin.gateway.core.route.predicate;

import com.muxin.gateway.core.route.exchange.DefaultHttpServerExchange;
import com.muxin.gateway.core.route.predicate.PredicateDefinition;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class PredicateBehaviorTest {

    @Test
    public void matchesConfiguredPathAndNegatedHeadersAndQueries() {
        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, "/API/items?id=42", Unpooled.EMPTY_BUFFER);
        request.headers().set(HttpHeaderNames.HOST, "example.test");
        DefaultHttpServerExchange exchange = new DefaultHttpServerExchange(request);

        PathPredicate path = new PathPredicate(PredicateDefinition.builder()
                .name("PATH")
                .args(Map.of("pattern", "/api/**", "ignoreCase", true))
                .build());
        HeaderPredicate missingHeader = new HeaderPredicate(PredicateDefinition.builder()
                .name("HEADER")
                .args(Map.of("header", "X-Missing", "not", true))
                .build());
        QueryPredicate missingQuery = new QueryPredicate(PredicateDefinition.builder()
                .name("QUERY")
                .args(Map.of("param", "missing", "not", true))
                .build());

        assertTrue(path.test(exchange));
        assertTrue(missingHeader.test(exchange));
        assertTrue(missingQuery.test(exchange));
        assertFalse(new QueryPredicate("id", "41").test(exchange));
    }

    @Test
    public void stripsConfiguredPrefixSegments() {
        assertEquals("/api/users/123", new PathPredicate("/api/**", 0).stripPrefix("/api/users/123"));
        assertEquals("/users/123", new PathPredicate("/api/**", 1).stripPrefix("/api/users/123"));
        assertEquals("/123", new PathPredicate("/api/**", 2).stripPrefix("/api/users/123"));
        assertEquals("/", new PathPredicate("/api/**", 5).stripPrefix("/api/users/123"));
    }
}
