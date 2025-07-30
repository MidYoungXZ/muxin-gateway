package com.muxin.gateway.core.route;

import com.muxin.gateway.core.route.filter.FilterDefinition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.support.BeanDefinitionValidationException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 路由定义
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouteDefinition {

    /**
     * 路由id
     */
    private String id;

    /**
     * 路由uri
     */
    private URI uri;

    /**
     * 路由断言集合
     */
    @Builder.Default
    private List<PredicateDefinition> predicates = new ArrayList<>();

    /**
     * 路由过滤器集合
     */
    @Builder.Default
    private List<FilterDefinition> filters = new ArrayList<>();

    /**
     * 路由元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();

    /**
     * 路由顺序
     */
    @Builder.Default
    private int order = 0;

    public RouteDefinition(String text) {
        int eqIdx = text.indexOf('=');
        if (eqIdx <= 0) {
            throw new BeanDefinitionValidationException(
                    "Unable to parse RouteDefinition text '" + text + "'" + ", must be of the form name=value");
        }

        setId(text.substring(0, eqIdx));

        String[] args = tokenizeToStringArray(text.substring(eqIdx + 1), ",");

        setUri(URI.create(args[0]));

        for (int i = 1; i < args.length; i++) {
            this.predicates.add(new PredicateDefinition(args[i]));
        }
    }

    public String[] tokenizeToStringArray(String str, String delimiters) {
        return str.split(delimiters);
    }
}
